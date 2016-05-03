package com.elasticbox.jenkins.k8s.repositories.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.ReplicationControllerRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.ReplicationController;

@Singleton
public class ReplicationControllerRepositoryApiImpl implements ReplicationControllerRepository {

    @Inject
    KubernetesRepository kubeRepository;

    @Override
    public void create(String kubeName, String namespace, ReplicationController replController)
            throws RepositoryException {

        kubeRepository.getClient(kubeName).replicationControllers().inNamespace(namespace).create(replController);
    }
    
    @Override
    public void delete(String kubeName, String namespace, ReplicationController replController)
            throws RepositoryException {
        
        kubeRepository.getClient(kubeName).replicationControllers().inNamespace(namespace).delete(replController);
    }
}
