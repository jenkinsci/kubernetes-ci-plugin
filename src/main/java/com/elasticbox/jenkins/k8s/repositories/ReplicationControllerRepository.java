package com.elasticbox.jenkins.k8s.repositories;

import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.ReplicationController;

public interface ReplicationControllerRepository {

    void create(String kubeName, String namespace, ReplicationController replicationController)
            throws RepositoryException;

    void delete(String kubeName, String namespace, ReplicationController replController)
            throws RepositoryException;
}
