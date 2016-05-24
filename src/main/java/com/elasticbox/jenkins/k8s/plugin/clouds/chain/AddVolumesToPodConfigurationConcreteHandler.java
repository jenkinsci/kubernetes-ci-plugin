package com.elasticbox.jenkins.k8s.plugin.clouds.chain;

import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;

import java.util.logging.Logger;


public class AddVolumesToPodConfigurationConcreteHandler extends AbstractPodDeploymentConcreteHandler {

    private static final Logger LOGGER = Logger.getLogger(AddVolumesToPodConfigurationConcreteHandler.class.getName());


    private KubernetesRepository kubernetesRepository;

    /**
     * Its mission is to add the volumes to the Pod if needed.
     * @param deploymentContext
     * @throws Exception
     */

    @Override
    public void handle(JenkinsPodSlaveDeploymentContext deploymentContext) throws Exception{

        //Not implemented yet
    }



}
