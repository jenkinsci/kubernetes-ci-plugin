package com.elasticbox.jenkins.k8s.cfg;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AddSlaveToJenkinsCloud;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AddEnvironmentVariablesToPodConfiguration;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AddLabelsToPodConfiguration;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.CheckProvisioningAllowed;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.CreatePodFromPodConfiguration;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeployer;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.SelectSuitablePodConfiguration;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.WaitForPodToBeRunning;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.WaitForSlaveToBeOnline;

import com.elasticbox.jenkins.k8s.services.SlaveProvisioningService;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.SlaveProvisioningStep;
import com.elasticbox.jenkins.k8s.services.SlaveProvisioningServiceImpl;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.ReplicationControllerRepository;
import com.elasticbox.jenkins.k8s.repositories.ServiceRepository;
import com.elasticbox.jenkins.k8s.repositories.api.KubernetesRepositoryApiImpl;
import com.elasticbox.jenkins.k8s.repositories.api.PodRepositoryApiImpl;
import com.elasticbox.jenkins.k8s.repositories.api.ReplicationControllerRepositoryApiImpl;
import com.elasticbox.jenkins.k8s.repositories.api.ServiceRepositoryApiImpl;
import com.elasticbox.jenkins.k8s.repositories.api.charts.ChartRepositoryApiImpl;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubClientsFactory;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubClientsFactoryImpl;
import com.elasticbox.jenkins.k8s.repositories.api.kubeclient.KubernetesClientFactory;
import com.elasticbox.jenkins.k8s.repositories.api.kubeclient.KubernetesClientFactoryImpl;
import com.elasticbox.jenkins.k8s.services.ChartDeploymentService;
import com.elasticbox.jenkins.k8s.services.ChartDeploymentServiceImpl;

import hudson.Extension;

@Extension
public class GuiceBindings extends AbstractModule {

    @Override
    protected void configure() {

        bind(KubernetesClientFactory.class)
            .to(KubernetesClientFactoryImpl.class)
            .in(Singleton.class);

        bind(KubernetesRepository.class)
            .to(KubernetesRepositoryApiImpl.class)
            .in(Singleton.class);

        bind(PodRepository.class)
            .to(PodRepositoryApiImpl.class)
            .in(Singleton.class);

        bind(ServiceRepository.class)
            .to(ServiceRepositoryApiImpl.class)
            .in(Singleton.class);

        bind(ReplicationControllerRepository.class)
            .to(ReplicationControllerRepositoryApiImpl.class)
            .in(Singleton.class);

        bind(ChartDeploymentService.class)
            .to(ChartDeploymentServiceImpl.class)
            .in(Singleton.class);

        bind(ChartRepository.class)
            .to(ChartRepositoryApiImpl.class)
            .in(Singleton.class);

        bind(GitHubClientsFactory.class)
            .to(GitHubClientsFactoryImpl.class)
            .in(Singleton.class);

        bind(SlaveProvisioningService.class)
            .to(SlaveProvisioningServiceImpl.class)
            .in(Singleton.class);

        Multibinder<SlaveProvisioningStep> podCreationChainHandlers =
            Multibinder.newSetBinder(binder(), SlaveProvisioningStep.class);
        podCreationChainHandlers.addBinding().to(CheckProvisioningAllowed.class);
        podCreationChainHandlers.addBinding().to(SelectSuitablePodConfiguration.class);
        podCreationChainHandlers.addBinding().to(CreatePodFromPodConfiguration.class);
        podCreationChainHandlers.addBinding().to(AddLabelsToPodConfiguration.class);
        podCreationChainHandlers.addBinding().to(AddSlaveToJenkinsCloud.class);
        podCreationChainHandlers.addBinding().to(AddEnvironmentVariablesToPodConfiguration.class);
        podCreationChainHandlers.addBinding().to(PodDeployer.class);
        podCreationChainHandlers.addBinding().to(WaitForPodToBeRunning.class);
        podCreationChainHandlers.addBinding().to(WaitForSlaveToBeOnline.class);


    }
}
