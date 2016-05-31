package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfigurationParams;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CreatePodFromPodConfiguration extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(CreatePodFromPodConfiguration.class.getName());

    @Inject
    private PodRepository podRepository;

    /**
     * Its mission is to check if we can provision one slave more. It will be impossible if we have already reached
     * the specific limit for the cloud
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        final KubernetesCloud cloudToDeployInto = deploymentContext.getCloudToDeployInto();

        final PodSlaveConfigurationParams podConfigurationChosen = deploymentContext.getPodConfigurationChosen();

        try {

            final Pod pod = podRepository.pod(
                cloudToDeployInto.name,
                cloudToDeployInto.getNamespace(),
                podConfigurationChosen.getPodYaml());

            StringBuilder builder =  new StringBuilder(pod.getMetadata().getName());
            builder.append("-");
            builder.append(Long.toHexString(System.nanoTime()));

            pod.getMetadata().setName(builder.toString());

            deploymentContext.setPodToDeploy(pod);

            LOGGER.log(Level.INFO, "Pod object model created from PodConfiguration with name: " + builder.toString());

        } catch (RepositoryException exception) {

            LOGGER.log(Level.SEVERE, "Error creating Pod model object");

            throw new ServiceException("Error creating Pod model object", exception);
        }

    }
}

