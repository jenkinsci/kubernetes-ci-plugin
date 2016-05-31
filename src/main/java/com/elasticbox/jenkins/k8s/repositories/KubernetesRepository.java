package com.elasticbox.jenkins.k8s.repositories;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloudParams;
import com.elasticbox.jenkins.k8s.util.KeyValuePair;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.List;

public interface KubernetesRepository {

    boolean testConnection(KubernetesCloudParams kubeCloudParams) throws RepositoryException;

    boolean testConnection(String kubernetesUri) throws RepositoryException;

    KubernetesClient getClient(String kubeName) throws RepositoryException;

    boolean namespaceExists(String kubeName, String namespace) throws RepositoryException;

    Namespace createNamespece(String kubeName, String namespace, KeyValuePair<String, String>... labels)
            throws RepositoryException;

    List<String> getNamespaces(String kubeName);

    List<String> getNamespaces(KubernetesCloudParams kubeCloudParams);
}
