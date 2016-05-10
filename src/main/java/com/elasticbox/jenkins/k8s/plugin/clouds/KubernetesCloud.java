package com.elasticbox.jenkins.k8s.plugin.clouds;

import static hudson.init.InitMilestone.PLUGINS_STARTED;

import com.google.inject.Inject;
import com.google.inject.Injector;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.plugin.util.PluginHelper;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.api.kubeclient.KubernetesClientFactory;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.Extension;
import hudson.init.Initializer;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.slaves.AbstractCloudImpl;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KubernetesCloud extends AbstractCloudImpl {

    private static final Logger LOGGER = Logger.getLogger(KubernetesCloud.class.getName() );
    private static final String NAME_PREFIX = "KubeCloud-";

    private final String description;
    private final String credentialsId;
    private final KubernetesCloudParams kubeCloudParams;
    private final List<ChartRepositoryConfig> chartRepositoryConfigurations;
    private final List<PodSlaveConfig> podSlaveConfigurations;

    @Inject
    transient KubernetesClientFactory kubeFactory;

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

    @Initializer (after = PLUGINS_STARTED)
    public static void checkLocalKubernetesCloud() throws IOException {
        LOGGER.info(NAME_PREFIX + "Checking if running inside a Kubernetes Cloud (Auto-discovery)...");
        KubernetesClient client = new DefaultKubernetesClient();
        try {
            client.namespaces().withName("default").get();
            LOGGER.info(NAME_PREFIX + "Kubernetes Cloud found! Local Kubernetes cloud will be configured");
            final String name = NAME_PREFIX + "Local";
            if (Jenkins.getInstance().getCloud(name) == null) {
                final KubernetesCloud cloud = new KubernetesCloud(name, name, client.getMasterUrl().toExternalForm(),
                        "default", "30", "", true, "", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
                Jenkins.getInstance().clouds.add(cloud);
            }
        } catch (KubernetesClientException exception) {
            LOGGER.info(NAME_PREFIX + "No Kubernetes Cloud found");
        }
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
        // TODO Deploy Slave
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean canProvision(Label label) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Label to check for provisioning: " + label);
        }
        // TODO
        return label != null;
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
                                                 @QueryParameter boolean disableCertCheck,
                                                 @QueryParameter String serverCert) {

            if (StringUtils.isEmpty(endpointUrl) ) {
                return PluginHelper.doFillNamespaceItems(null);
            }

            Authentication authData = PluginHelper.getAuthenticationData(credentialsId);
            final KubernetesCloudParams kubeCloudParams =
                    new KubernetesCloudParams(endpointUrl, null, authData, disableCertCheck, serverCert);

            return PluginHelper.doFillNamespaceItems(kubeRepository.getNamespaces(kubeCloudParams) );
        }

        public FormValidation doTestConnection(@QueryParameter String endpointUrl,
                                               @QueryParameter String namespace,
                                               @QueryParameter String credentialsId,
                                               @QueryParameter boolean disableCertCheck,
                                               @QueryParameter String serverCert) {

            if (StringUtils.isEmpty(endpointUrl) ) {
                return FormValidation.error("Required fields not provided");
            }

            Authentication authData = PluginHelper.getAuthenticationData(credentialsId);
            final KubernetesCloudParams kubeCloudParams = new KubernetesCloudParams(
                    endpointUrl, namespace, authData, disableCertCheck, serverCert);

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
