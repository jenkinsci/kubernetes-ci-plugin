package com.elasticbox.jenkins.k8s.repositories.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.ServiceRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Service;

@Singleton
public class ServiceRepositoryApiImpl implements ServiceRepository {

    @Inject
    KubernetesRepository kubeRepository;

    @Override
    public void create(String kubeName, String namespace, Service service) throws RepositoryException {
        kubeRepository.getClient(kubeName).services().inNamespace(namespace).create(service);
    }

    @Override
    public void delete(String kubeName, String namespace, Service service) throws RepositoryException {
        kubeRepository.getClient(kubeName).services().inNamespace(namespace).delete(service);
    }
}
