package com.elasticbox.jenkins.k8s.plugin.clouds.chain;

import com.elasticbox.jenkins.k8s.plugin.clouds.slaves.JenkinsKubernetesSlave;
import com.google.common.collect.ImmutableMap;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;

import io.fabric8.kubernetes.api.model.Pod;
import jenkins.model.Jenkins;

import java.util.Map;
import java.util.logging.Logger;


public class PodDeployerConcreteHandler extends AbstractPodDeploymentConcreteHandler {

    private static final Logger LOGGER = Logger.getLogger(PodDeployerConcreteHandler.class.getName());

    /** label for all pods started by the plugin */
    public static final Map<String, String> POD_SLAVE_KUBERNETES_LABEL = ImmutableMap.of("jenkins", "slave");


    private PodRepository podRepository;

    public PodDeployerConcreteHandler(PodRepository podRepository) {
        this.podRepository = podRepository;
    }

    @Override
    public void handle(JenkinsPodSlaveDeploymentContext deploymentContext) throws RepositoryException {


        ///TODO
//        JenkinsKubernetesSlave slave = new JenkinsKubernetesSlave(t, getIdForLabel(label), cloud, label);
//
//        Jenkins.getInstance().addNode(slave);



        final Pod podToDeploy = deploymentContext.getPodToDeploy();

        final KubernetesCloud cloudToDeployInto = deploymentContext.getCloudToDeployInto();

        final String deploymentNamespace = deploymentContext.getDeploymentNamespace();

        podRepository.create(cloudToDeployInto.name, deploymentNamespace, podToDeploy);

    }
}
