package com.elasticbox.jenkins.k8s.plugin.clouds.chain;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;


public class CheckProvisioningAllowedConcreteHandler extends AbstractPodDeploymentConcreteHandler {

    private static final Logger LOGGER = Logger.getLogger(CheckProvisioningAllowedConcreteHandler.class.getName());


    private KubernetesRepository kubernetesRepository;

    public CheckProvisioningAllowedConcreteHandler(KubernetesRepository kubernetesRepository) {
        this.kubernetesRepository = kubernetesRepository;
    }

    /**
     * Its mission is to check if we can provision one slave more. It will be impossible if we have already reached
     * the specific limit for the cloud
     * @param deploymentContext
     * @throws Exception
     */

    @Override
    public void handle(JenkinsPodSlaveDeploymentContext deploymentContext) throws RepositoryException{

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

        final KubernetesClient kubernetesClient = kubernetesRepository.getClient(cloudToDeployInto.name);

        PodList list = kubernetesClient.pods().inNamespace(deploymentNamespace).list();

        if (list.getItems().size() >= cloudCapacity) {
            String message = "Not provisioning, max cloud capacity: " + cloudCapacity + "reached";
            LOGGER.log(Level.SEVERE, message);
            throw new RuntimeException(message);
        }

    }



}
