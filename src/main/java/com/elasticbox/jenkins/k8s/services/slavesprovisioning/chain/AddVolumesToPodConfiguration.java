package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain;

import com.elasticbox.jenkins.k8s.services.error.ServiceException;

import java.util.logging.Logger;


public class AddVolumesToPodConfiguration extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(AddVolumesToPodConfiguration.class.getName());

    /**
     * Its mission is to add the volumes to the Pod if needed.
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {
        //Not implemented yet
    }



}
