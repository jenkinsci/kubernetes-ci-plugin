package com.elasticbox.jenkins.k8s.repositories;

import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.ReplicationController;

import java.util.Map;

public interface ReplicationControllerRepository {

    void create(String kubeName, String namespace, ReplicationController replicationController)
            throws RepositoryException;

    void create(String kubeName, String namespace, ReplicationController controller, Map<String, String> labels)
            throws RepositoryException;

    void delete(String kubeName, String namespace, ReplicationController replController)
            throws RepositoryException;
}
