package com.elasticbox.jenkins.k8s.repositories;

import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.Map;

public interface PodRepository {

    void create(String kubeName, String namespace, Pod pod) throws RepositoryException;

    void create(String kubeName, String namespace, Pod pod, Map<String, String> labels) throws RepositoryException;

    void delete(String kubeName, String namespace, Pod pod) throws RepositoryException;

    void delete(String kubeName, String namespace, String podName) throws RepositoryException;

    Pod pod(String kubeName, String namespace, String yaml) throws RepositoryException;
}
