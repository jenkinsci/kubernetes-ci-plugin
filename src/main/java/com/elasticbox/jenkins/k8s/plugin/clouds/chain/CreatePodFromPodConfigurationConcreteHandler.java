package com.elasticbox.jenkins.k8s.plugin.clouds.chain;

import com.elasticbox.jenkins.k8s.pod.PodConfiguration;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.logging.Logger;


public class CreatePodFromPodConfigurationConcreteHandler extends AbstractPodDeploymentConcreteHandler {

    private static final Logger LOGGER = Logger.getLogger(CreatePodFromPodConfigurationConcreteHandler.class.getName());


    private KubernetesRepository kubernetesRepository;

    /**
     * Its mission is to check if we can provision one slave more. It will be impossible if we have already reached
     * the specific limit for the cloud
     * @param deploymentContext
     * @throws Exception
     */

    @Override
    public void handle(JenkinsPodSlaveDeploymentContext deploymentContext) throws RepositoryException{

        final PodConfiguration podConfigurationChosen = deploymentContext.getPodConfigurationChosen();

        final String yamlPod = podConfigurationChosen.getYamlPod();

        final InputStream inputStream = IOUtils.toInputStream(yamlPod);

        final Pod pod = new DefaultKubernetesClient().pods().load(inputStream).get();

        deploymentContext.setPodToDeploy(pod);

    }
}

