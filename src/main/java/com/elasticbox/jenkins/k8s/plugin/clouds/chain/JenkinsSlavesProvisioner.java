package com.elasticbox.jenkins.k8s.plugin.clouds.chain;

import com.elasticbox.jenkins.k8s.plugin.clouds.slaves.JenkinsKubernetesSlave;
import com.google.inject.Inject;

import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.pod.PodConfiguration;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;

import hudson.Extension;
import hudson.model.Label;

import java.util.List;

@Extension
public class JenkinsSlavesProvisioner {

    @Inject
    private PodRepository podRepository;

    @Inject
    private KubernetesRepository kubernetesRepository;

    private SlavePodCreationConcreteHandler [] podCreationChainHandlers;


    public JenkinsSlavesProvisioner() {

        this.podCreationChainHandlers = new SlavePodCreationConcreteHandler[] {
            new CheckProvisioningAllowedConcreteHandler(kubernetesRepository),
            new SelectSuitablePodConfigurationConcreteHandler(),
            new AddEnvironmentVariablesToPodConfigurationConcreteHandler(),
            new CreatePodFromPodConfigurationConcreteHandler(),
            new PodDeployerConcreteHandler(podRepository)
        };
    }



    public JenkinsKubernetesSlave slaveProvision(KubernetesCloud kubernetesCloud,
                                                 String namespace,
                                                 List<PodConfiguration> podConfigurations,
                                                 Label label) throws ServiceException, RepositoryException {

        JenkinsPodSlaveDeploymentContext deploymentContext =
            new JenkinsPodSlaveDeploymentContext.JenkinsPodSlaveDeploymentContextBuilder()
                                                .withJobLabel(label)
                                                .withOneOfThesePodConfigurations(podConfigurations)
                                                .intoKubernetesCloud(kubernetesCloud)
                                                .withNamespace(namespace)
                                                .build();

        for (SlavePodCreationConcreteHandler deploymentHandler: podCreationChainHandlers) {
            deploymentHandler.handle(deploymentContext);
        }

        final JenkinsKubernetesSlave jenkinsKubernetesSlave = deploymentContext.getJenkinsKubernetesSlave();

        return jenkinsKubernetesSlave;

    }


}
