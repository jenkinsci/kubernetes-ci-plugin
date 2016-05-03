package com.elasticbox.jenkins.k8s.repositories;

import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Service;

public interface ServiceRepository {

    public void create(String kubeName, String namespace, Service service) throws RepositoryException;

    void delete(String kubeName, String namespace, Service service)
            throws RepositoryException;
}
