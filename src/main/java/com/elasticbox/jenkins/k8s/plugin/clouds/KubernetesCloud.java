package com.elasticbox.jenkins.k8s.plugin.clouds;

import com.google.inject.Inject;
import com.google.inject.Injector;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.plugin.util.PluginHelper;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.api.kubeclient.KubernetesClientFactory;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudImpl;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.Cloud;
import hudson.slaves.ComputerListener;
import hudson.slaves.NodeProvisioner;
import hudson.slaves.OfflineCause;
import hudson.slaves.SlaveComputer;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.fabric8.kubernetes.api.model.Pod;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KubernetesCloud extends AbstractCloudImpl {

    private static final Logger LOGGER = Logger.getLogger(KubernetesCloud.class.getName() );
    public static final String NAME_PREFIX = "KubeCloud-";

    private final String description;
    private final String credentialsId;
    private final KubernetesCloudParams kubeCloudParams;
    private final List<ChartRepositoryConfig> chartRepositoryConfigurations;
    private final List<PodSlaveConfig> podSlaveConfigurations;

    @Inject
    transient KubernetesClientFactory kubeFactory;

    @Inject
    transient PodRepository podRepository;

    @DataBoundConstructor
    public KubernetesCloud(String name, String description, String endpointUrl, String namespace,
                           String maxContainers, String credentialsId, boolean disableCertCheck, String serverCert,
                           List<ChartRepositoryConfig> chartRepositoryConfigurations,
                           List<PodSlaveConfig> podSlaveConfigurations) {

        super( (StringUtils.isNotEmpty(name) ) ? name : NAME_PREFIX + UUID.randomUUID().toString(), maxContainers );
        this.description = description;
        this.credentialsId = credentialsId;

        // Passing !disableCertCheck because it is with 'negative' property in jelly (opposite behaviour)
        this.kubeCloudParams = new KubernetesCloudParams(endpointUrl, namespace,
                PluginHelper.getAuthenticationData(credentialsId), !disableCertCheck, serverCert);

        this.chartRepositoryConfigurations = chartRepositoryConfigurations;
        this.podSlaveConfigurations = podSlaveConfigurations;

        injectMembers();
        if (StringUtils.isNotEmpty(name) ) {
            kubeFactory.resetKubernetesClient(name);
        }
    }

    public static List<KubernetesCloud> getKubernetesClouds() {
        List<KubernetesCloud> clouds = new ArrayList<>();

        final Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            for (Cloud cloud : instance.clouds) {
                if (cloud instanceof KubernetesCloud) {
                    clouds.add( (KubernetesCloud) cloud);
                }
            }
        }
        return clouds;
    }

    public static KubernetesCloud getKubernetesCloud(String kubeName) {
        final Jenkins instance = Jenkins.getInstance();

        if (instance == null) {
            return null;
        }
        final Cloud cloud = instance.getCloud(kubeName);
        return (cloud != null && cloud instanceof KubernetesCloud) ? (KubernetesCloud)cloud : null;
    }

    public String getDescription() {
        return description;
    }

    public String getNamespace() {
        return kubeCloudParams.getNamespace();
    }

    public String getEndpointUrl() {
        return kubeCloudParams.getEndpointUrl();
    }

    public String getMaxContainers() {
        return super.getInstanceCapStr();
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public boolean getDisableCertCheck() {
        return kubeCloudParams.isDisableCertCheck();
    }

    public List<ChartRepositoryConfig> getChartRepositoryConfigurations() {
        return chartRepositoryConfigurations;
    }

    public List<PodSlaveConfig> getPodSlaveConfigurations() {
        return podSlaveConfigurations;
    }

    public ChartRepositoryConfig getChartRepositoryConfiguration(String chartsRepo) {
        for (ChartRepositoryConfig config : getChartRepositoryConfigurations() ) {
            if (config.getDescription().equals(chartsRepo)) {
                return config;
            }
        }
        return null;
    }

    @DataBoundSetter
    public void setDisableCertCheck(boolean disableCertCheck) {
        this.kubeCloudParams.setDisableCertCheck(disableCertCheck);
    }

    @Override
    public String getDisplayName() {
        return StringUtils.isNotBlank(description) ? description : kubeCloudParams.getEndpointUrl();
    }

    @Override
    public synchronized Collection<NodeProvisioner.PlannedNode> provision(Label label, int excessWorkload) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Provisioning label [" + label + "] with excessWorkload: " + excessWorkload);
        }

        if (podSlaveConfigurations == null || podSlaveConfigurations.size() == 0) {
            LOGGER.warning("No slave configuration available to deploy slave!");
            return Collections.EMPTY_LIST;
        }

        String jenkinsUrl = JenkinsLocationConfiguration.get().getUrl();
        PodSlaveConfig podSlaveConfig = podSlaveConfigurations.get(0);
        String podYaml = podSlaveConfig.getPodYaml().replace("${JENKINS_URL}", jenkinsUrl);

        int index = Jenkins.getInstance().getNodes().size();
        while (excessWorkload > 0) {
            try {
                Pod pod = podRepository.getPod(name, kubeCloudParams.getNamespace(), podYaml);

                pod.getMetadata().setName(pod.getMetadata().getName() + "-" + ++index );

                Map<String, String> labels = pod.getMetadata().getLabels();
                labels.put("jenkins-label", (label != null) ? label.toString() : "NONE" );
                pod.getMetadata().setLabels(labels);
                LOGGER.info("Generated Pod:" + pod);

                podRepository.create(name, kubeCloudParams.getNamespace(), pod);
                LOGGER.info("Pod created as slave:" + pod);

                excessWorkload --;

            } catch (RepositoryException exc) {
                exc.printStackTrace();
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean canProvision(Label label) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Label to check for provisioning: " + label);
        }
        // TODO
        return true;//label != null;
    }


    public KubernetesCloudParams getKubernetesCloudParams() {
        return kubeCloudParams;
    }

    @Override
    public String toString() {
        return "KubernetesCloud [" + getDescription() + "@" + kubeCloudParams.getEndpointUrl() + "]";
    }

    protected Object readResolve() {
        injectMembers();
        return this;
    }

    private void injectMembers() {
        if (kubeFactory == null) {
            ( (DescriptorImpl) getDescriptor() ).injector.injectMembers(this);
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Cloud> {
        private static final String KUBERNETES_CLOUD = "Kubernetes cloud";

        @Inject
        private Injector injector;

        @Inject
        KubernetesRepository kubeRepository;

        @Override
        public String getDisplayName() {
            return KUBERNETES_CLOUD;
        }

        public Injector getInjector() {
            return injector;
        }

        public ListBoxModel doFillNamespaceItems(@QueryParameter String endpointUrl,
                                                 @QueryParameter String credentialsId,
                                                 @QueryParameter String serverCert) {

            if (StringUtils.isEmpty(endpointUrl) ) {
                return PluginHelper.doFillNamespaceItems(null);
            }

            Authentication authData = PluginHelper.getAuthenticationData(credentialsId);
            final KubernetesCloudParams kubeCloudParams =
                    new KubernetesCloudParams(endpointUrl, null, authData, true, serverCert);

            return PluginHelper.doFillNamespaceItems(kubeRepository.getNamespaces(kubeCloudParams) );
        }

        public FormValidation doTestConnection(@QueryParameter String endpointUrl,
                                               @QueryParameter String namespace,
                                               @QueryParameter String credentialsId,
                                               @QueryParameter String serverCert) {

            if (StringUtils.isEmpty(endpointUrl) ) {
                return FormValidation.error("Required fields not provided");
            }

            Authentication authData = PluginHelper.getAuthenticationData(credentialsId);
            final KubernetesCloudParams kubeCloudParams = new KubernetesCloudParams(
                    endpointUrl, namespace, authData, false, serverCert);

            try {
                if (kubeRepository.testConnection(kubeCloudParams) ) {
                    if (LOGGER.isLoggable(Level.CONFIG) ) {
                        LOGGER.config("Connection successful to Kubernetes Cloud at: " + endpointUrl);
                    }
                    return FormValidation.ok("Connection successful");
                } else {
                    LOGGER.severe("Unable to connect to Kubernetes clouds at: " + endpointUrl);
                    return FormValidation.error("Connection error");
                }
            } catch (RepositoryException excep) {
                String msg = "Connection error - " + excep.getCausedByMessages();
                if (excep.getCause() != null && excep.getCause().getCause() instanceof SSLHandshakeException) {
                    kubeCloudParams.setDisableCertCheck(true);
                    try {
                        if (kubeRepository.testConnection(kubeCloudParams) ) {
                            if (LOGGER.isLoggable(Level.CONFIG) ) {
                                LOGGER.config("Connection successful disabling Cert check to Kubernetes Cloud at: "
                                        + endpointUrl);
                            }
                            return FormValidation.warning(
                                    "Warning: Connection successful but the server certificate is not trusted");
                        }
                    } catch (RepositoryException exception) {
                        msg = "Connection error - " + exception.getCausedByMessages();
                    }
                }
                LOGGER.severe(msg);
                return FormValidation.error(msg);
            }
        }

        public ListBoxModel doFillCredentialsIdItems(@QueryParameter String endpointUrl) {
            return PluginHelper.doFillCredentialsIdItems(endpointUrl);
        }

        public FormValidation doCheckEndpointUrl(@QueryParameter String endpointUrl) {
            if (StringUtils.isBlank(endpointUrl)) {
                return FormValidation.error("Endpoint Url is required");
            }
            return FormValidation.ok();
        }
    }

}
