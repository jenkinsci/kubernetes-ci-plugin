package com.elasticbox.jenkins.k8s.repositories.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.ReplicationControllerRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.DoneableReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ClientRollableScallableResource;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ReplicationControllerRepositoryApiImpl implements ReplicationControllerRepository {
    private static final Logger LOGGER = Logger.getLogger(ReplicationControllerRepositoryApiImpl.class.getName() );

    @Inject
    KubernetesRepository kubeRepository;

    @Override
    public void create(String kubeName, String namespace, ReplicationController replController)
            throws RepositoryException {

        if (LOGGER.isLoggable(Level.CONFIG) ) {
            LOGGER.config("Creating Replication Controller: " + replController.getMetadata().getName() );
        }
        kubeRepository.getClient(kubeName).replicationControllers().inNamespace(namespace).create(replController);
    }
    
    @Override
    public void delete(String kubeName, String namespace, ReplicationController replController)
            throws RepositoryException {

        if (LOGGER.isLoggable(Level.CONFIG) ) {
            LOGGER.config("Deleting Replication Controller and associated Pods: "
                    + replController.getMetadata().getName() );
        }

        final KubernetesClient client = kubeRepository.getClient(kubeName);
        client.replicationControllers().inNamespace(namespace)
                .withName(replController.getMetadata().getName() )
                .edit().editSpec()
                    .withReplicas(0)
                .endSpec().done();

        client.replicationControllers().inNamespace(namespace).delete(replController);
    }
}
