package com.elasticbox.jenkins.k8s.repositories;

import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Service;

import java.util.Map;

public interface ServiceRepository {

    void create(String kubeName, String namespace, Service service) throws RepositoryException;

    void create(String kubeName, String namespace, Service service, Map<String, String> labels)
            throws RepositoryException;

    void delete(String kubeName, String namespace, Service service)
            throws RepositoryException;
}
