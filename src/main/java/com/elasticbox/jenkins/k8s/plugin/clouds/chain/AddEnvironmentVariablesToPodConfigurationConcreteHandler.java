package com.elasticbox.jenkins.k8s.plugin.clouds.chain;

import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfig;
import com.elasticbox.jenkins.k8s.pod.PodConfiguration;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import jenkins.model.JenkinsLocationConfiguration;

import java.util.logging.Logger;


public class AddEnvironmentVariablesToPodConfigurationConcreteHandler extends AbstractPodDeploymentConcreteHandler {

    private static final Logger LOGGER = Logger.getLogger(AddEnvironmentVariablesToPodConfigurationConcreteHandler.class.getName());


    private KubernetesRepository kubernetesRepository;

    /**
     * Its mission is to add values for the environment variables , if needed
     * @param deploymentContext
     * @throws Exception
     */

    @Override
    public void handle(JenkinsPodSlaveDeploymentContext deploymentContext) throws RepositoryException{

        String jenkinsUrl = JenkinsLocationConfiguration.get().getUrl();

        final PodConfiguration podConfigurationChosen = deploymentContext.getPodConfigurationChosen();

        final String yamlPod = podConfigurationChosen.getYamlPod();

        String podYaml = yamlPod.replace("${JENKINS_URL}", jenkinsUrl);

        podConfigurationChosen.setYamlPod(podYaml);

    }



}
