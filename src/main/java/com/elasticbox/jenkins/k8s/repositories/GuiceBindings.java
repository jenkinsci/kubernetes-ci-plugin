package com.elasticbox.jenkins.k8s.repositories;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

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
        bind(KubernetesClientFactory.class).to(KubernetesClientFactoryImpl.class).in(Singleton.class);
        bind(KubernetesRepository.class).to(KubernetesRepositoryApiImpl.class).in(Singleton.class);
        bind(PodRepository.class).to(PodRepositoryApiImpl.class).in(Singleton.class);
        bind(ServiceRepository.class).to(ServiceRepositoryApiImpl.class).in(Singleton.class);
        bind(ReplicationControllerRepository.class).to(ReplicationControllerRepositoryApiImpl.class)
                .in(Singleton.class);
        bind(ChartDeploymentService.class).to(ChartDeploymentServiceImpl.class).in(Singleton.class);
        bind(ChartRepository.class).to(ChartRepositoryApiImpl.class).in(Singleton.class);
        bind(GitHubClientsFactory.class).to(GitHubClientsFactoryImpl.class).in(Singleton.class);
    }
}
