package com.elasticbox.jenkins.k8s.cfg;

import static com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud.NAME_PREFIX;
import static com.elasticbox.jenkins.k8s.plugin.util.PluginHelper.DEFAULT_NAMESPACE;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.util.PluginHelper;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginInitializer {

    private static final Logger LOGGER = Logger.getLogger(PluginInitializer.class.getName() );

    private static final String MAX_SLAVES = "30";
    public static final String LOCAL_CLOUD_NAME = NAME_PREFIX + "Local";

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
            if ( !PluginHelper.checkKubernetesClientConnection(kubernetesUri) ) {
                LOGGER.warning(NAME_PREFIX + "No valid Local Kubernetes Cloud connection obtained.");
                return;
            }

            final KubernetesCloud cloud = new KubernetesCloud(LOCAL_CLOUD_NAME, LOCAL_CLOUD_NAME, kubernetesUri,
                    DEFAULT_NAMESPACE, MAX_SLAVES, StringUtils.EMPTY, false, StringUtils.EMPTY,
                    Collections.EMPTY_LIST, Collections.EMPTY_LIST);

            LOGGER.info(NAME_PREFIX + "Adding local Kubernetes Cloud configuration: " + cloud);

            Jenkins.getInstance().clouds.add(cloud);

        } catch (RepositoryException exception) {
            LOGGER.log(Level.SEVERE, NAME_PREFIX + exception.getCausedByMessages(), exception);
        }
    }


}
