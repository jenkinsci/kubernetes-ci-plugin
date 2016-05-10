package com.elasticbox.jenkins.k8s.cfg;

import static com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud.NAME_PREFIX;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.util.PluginHelper;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginInitializer {

    private static final Logger LOGGER = Logger.getLogger(PluginInitializer.class.getName() );

    private static final String DEFAULT_NAMESPACE = "default";
    private static final String MAX_SLAVES = "30";

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

        final String name = NAME_PREFIX + "Local";
        if (Jenkins.getInstance().getCloud(name) != null) {
            LOGGER.info(NAME_PREFIX + "Local Kubernetes Cloud already configured.");
            return;
        }

        try {

            final ConfigBuilder builder = new ConfigBuilder().withMasterUrl(kubernetesUri).withTrustCerts(true);
            KubernetesClient client = new DefaultKubernetesClient(builder.build() );

            client.namespaces().withName(DEFAULT_NAMESPACE).get();

            final KubernetesCloud cloud = new KubernetesCloud(name, name, kubernetesUri,
                    DEFAULT_NAMESPACE, MAX_SLAVES, StringUtils.EMPTY, false, StringUtils.EMPTY,
                    Collections.EMPTY_LIST, Collections.EMPTY_LIST);

            LOGGER.info(NAME_PREFIX + "Adding local Kubernetes Cloud configuration: " + cloud);

            Jenkins.getInstance().clouds.add(cloud);

        } catch (KubernetesClientException exception) {
            final RepositoryException repositoryException =
                    new RepositoryException("Error trying to auto-discover Kubernetes cloud: ", exception);

            LOGGER.log(Level.SEVERE, NAME_PREFIX + repositoryException.getCausedByMessages(), exception);
        }
    }
}
