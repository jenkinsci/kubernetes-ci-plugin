package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain;

import com.elasticbox.jenkins.k8s.services.error.ServiceException;

public interface SlaveProvisioningStep {

    void handle(PodDeploymentContext deploymentContext) throws ServiceException;

}
