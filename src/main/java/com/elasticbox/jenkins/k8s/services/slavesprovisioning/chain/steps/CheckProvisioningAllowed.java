package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AbstractPodDeployment;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CheckProvisioningAllowed extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(CheckProvisioningAllowed.class.getName());

    @Inject
    private KubernetesRepository kubernetesRepository;


    /**
     * Its mission is to check if we can provision one slave more. It will be impossible if we have already reached
     * the specific limit for the cloud
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        final KubernetesCloud cloudToDeployInto = deploymentContext.getCloudToDeployInto();

        final int cloudCapacity = cloudToDeployInto.getInstanceCap();

        if (cloudCapacity == 0) {
            //is not possible to deploy any slave in this kubernetes cloud
            String message = "It is impossible to deploy a slave in a cloud which capacity is zero";
            LOGGER.log(Level.SEVERE, message);
            throw new RuntimeException(message);
        }

        String deploymentNamespace = StringUtils.isNotBlank(deploymentContext.getDeploymentNamespace())
            ? deploymentContext.getDeploymentNamespace()
            : cloudToDeployInto.getNamespace();

        final KubernetesClient kubernetesClient;
        try {

            kubernetesClient = kubernetesRepository.getClient(cloudToDeployInto.name);

            PodList list = kubernetesClient.pods().inNamespace(deploymentNamespace).list();

            if (list.getItems().size() >= cloudCapacity) {
                String message = "Not provisioning, max cloud capacity: " + cloudCapacity + "reached";
                LOGGER.log(Level.SEVERE, message);
                throw new ServiceException(message);
            }

            LOGGER.log(Level.INFO, "Pod deployment granted, cloud capacity: " + list.getItems().size());

        } catch (RepositoryException e) {

            LOGGER.log(Level.SEVERE, "Error getting the kubernetes client for the cloud " +  cloudToDeployInto.name);

            throw new ServiceException("Error getting the kubernetes client for the cloud " +  cloudToDeployInto.name);
        }


    }



}
