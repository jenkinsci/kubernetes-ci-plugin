package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AbstractPodDeployment;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PodDeployer extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(PodDeployer.class.getName());

    /** label for all pods started by the plugin. */
    public static final Map<String, String> POD_SLAVE_KUBERNETES_LABEL = ImmutableMap.of("jenkins", "slave");

    @Inject
    private PodRepository podRepository;

    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        final Pod podToDeploy = deploymentContext.getPodToDeploy();

        final KubernetesCloud cloudToDeployInto = deploymentContext.getCloudToDeployInto();

        final String deploymentNamespace = deploymentContext.getDeploymentNamespace();

        try {

            podRepository.create(cloudToDeployInto.getName(), deploymentNamespace, podToDeploy);

            String podName = podToDeploy.getMetadata().getName();

            LOGGER.log(Level.INFO, "Pod: " + podName + " created");

        } catch (RepositoryException exception) {
            String message = "Error getting the Kubernetes client for the cloud " + cloudToDeployInto.getName() ;
            LOGGER.log(Level.SEVERE, message);
            throw new ServiceException(message, exception);
        }

    }
}
