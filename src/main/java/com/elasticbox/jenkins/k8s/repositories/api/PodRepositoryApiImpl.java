package com.elasticbox.jenkins.k8s.repositories.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Pod;

@Singleton
public class PodRepositoryApiImpl implements PodRepository {

    @Inject
    KubernetesRepository kubeRepository;

    @Override
    public void create(String kubeName, String namespace, Pod pod) throws RepositoryException {
        kubeRepository.getClient(kubeName).pods().inNamespace(namespace).create(pod);
    }

    @Override
    public void delete(String kubeName, String namespace, Pod pod) throws RepositoryException {
        kubeRepository.getClient(kubeName).pods().inNamespace(namespace).delete(pod);
    }
}

