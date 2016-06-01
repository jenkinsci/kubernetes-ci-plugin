package com.elasticbox.jenkins.k8s.cfg;

import static com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud.NAME_PREFIX;
import static com.elasticbox.jenkins.k8s.util.PluginHelper.DEFAULT_NAMESPACE;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.elasticbox.jenkins.k8s.plugin.auth.TokenCredentials;
import com.elasticbox.jenkins.k8s.plugin.auth.TokenCredentialsImpl;
import com.elasticbox.jenkins.k8s.plugin.clouds.ChartRepositoryConfig;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfig;
import com.elasticbox.jenkins.k8s.util.PluginHelper;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.api.KubernetesRepositoryApiImpl;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginInitializer {

    private static final Logger LOGGER = Logger.getLogger(PluginInitializer.class.getName() );

    private static final String MAX_SLAVES = "30";
    private static final String DEFAULT_HELM_CHART_REPO = "Default Helm charts repository";
    private static final String DEFAULT_HELM_CHART_REPO_URL = "https://github.com/helm/charts";

    public static final String LOCAL_CLOUD_NAME = NAME_PREFIX + "Local";
    private static final String DEFAULT_JENKINS_SLAVE_POD_YAML = "default-jenkins-slave-pod.yaml";
    private static final String KUBE_SERVICE_TOKEN_ID = "KubeServiceToken";

    static String KUBE_TOKEN_PATH = "/run/secrets/kubernetes.io/serviceaccount/token";

    static KubernetesRepository kubeRepository = new KubernetesRepositoryApiImpl();

    @Initializer(after = InitMilestone.JOB_LOADED)
    public static void checkLocalKubernetesCloud() {

        LOGGER.info(NAME_PREFIX + "Checking if running inside a Kubernetes Cloud (Auto-discovery)...");

        final String kubernetesAddr = System.getenv("KUBERNETES_PORT_443_TCP_ADDR");
        final String kubernetesAddrPort = System.getenv("KUBERNETES_PORT_443_TCP_PORT");

        if (PluginHelper.anyOfThemIsBlank(kubernetesAddr, kubernetesAddrPort)) {
            LOGGER.info(NAME_PREFIX + "Not running inside a Kubernetes Cloud.");
            return;
        }

        String kubernetesUri = "https://" + kubernetesAddr + ":" + kubernetesAddrPort;

        LOGGER.info(NAME_PREFIX
                + "Kubernetes Cloud found! Checking if local Kubernetes cloud is configured at: "
                + kubernetesUri);

        if (Jenkins.getInstance().getCloud(LOCAL_CLOUD_NAME) != null) {
            LOGGER.info(NAME_PREFIX + "Local Kubernetes Cloud already configured.");
            return;
        }

        try {
            if ( !kubeRepository.testConnection(kubernetesUri) ) {
                LOGGER.warning(NAME_PREFIX + "No valid Local Kubernetes Cloud connection obtained.");
                return;
            }

            final ChartRepositoryConfig chartRepositoryConfig =
                    new ChartRepositoryConfig(DEFAULT_HELM_CHART_REPO, DEFAULT_HELM_CHART_REPO_URL, StringUtils.EMPTY);

            final PodSlaveConfig defaultPodSlaveConfig = getDefaultPodSlaveConfig();

            String credentialsId = checkLocalKubernetesToken();

            final KubernetesCloud cloud = new KubernetesCloud(LOCAL_CLOUD_NAME, LOCAL_CLOUD_NAME, kubernetesUri,
                    DEFAULT_NAMESPACE, MAX_SLAVES, credentialsId, StringUtils.EMPTY,
                    Collections.singletonList(chartRepositoryConfig),
                    (defaultPodSlaveConfig != null) ? Collections.singletonList(defaultPodSlaveConfig)
                            : Collections.EMPTY_LIST);

            LOGGER.info(NAME_PREFIX + "Adding local Kubernetes Cloud configuration: " + cloud);

            Jenkins.getInstance().clouds.add(cloud);

        } catch (RepositoryException exception) {
            LOGGER.log(Level.SEVERE, NAME_PREFIX + exception.getCausedByMessages(), exception);
        }
    }

    private static String checkLocalKubernetesToken() {
        File file = new File(KUBE_TOKEN_PATH);
        if (file.exists() && file.canRead() ) {
            try {
                String token = IOUtils.toString(file.toURI() );
                TokenCredentialsImpl kubeServiceToken = new TokenCredentialsImpl(CredentialsScope.SYSTEM,
                        KUBE_SERVICE_TOKEN_ID, "Local Kubernetes service token", Secret.fromString(token) );

                SystemCredentialsProvider.getInstance().getCredentials().add(kubeServiceToken);
                SystemCredentialsProvider.getInstance().save();

                return KUBE_SERVICE_TOKEN_ID;

            } catch (IOException excep) {
                LOGGER.warning("Unable to read Kubernetes service token file: " + excep);
            }
        }
        return StringUtils.EMPTY;
    }

    private static PodSlaveConfig getDefaultPodSlaveConfig() {

        final InputStream yamlStream =
                PodSlaveConfig.class.getResourceAsStream("PodSlaveConfig/" + DEFAULT_JENKINS_SLAVE_POD_YAML);

        final String yamlString;
        try {
            yamlString = IOUtils.toString(yamlStream);
        } catch (IOException exception) {
            LOGGER.severe(NAME_PREFIX + exception);
            return null;
        }

        if (LOGGER.isLoggable(Level.CONFIG) ) {
            LOGGER.config("Default Pod Yaml definition: " + yamlString);
        }

        final PodSlaveConfig podSlaveConfig =
                new PodSlaveConfig(DEFAULT_JENKINS_SLAVE_POD_YAML, "Default JenkinsSlave Pod", yamlString, null);

        return podSlaveConfig;
    }
}
