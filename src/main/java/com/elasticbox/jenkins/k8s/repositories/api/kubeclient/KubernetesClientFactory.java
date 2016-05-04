package com.elasticbox.jenkins.k8s.repositories.api.kubeclient;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloudParams;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.client.KubernetesClient;

public interface KubernetesClientFactory {

    KubernetesClient getKubernetesClient(String kubeName) throws RepositoryException;

    KubernetesClient createKubernetesClient(KubernetesCloudParams kubeCloudParams);

    void resetKubernetesClient(String kubeName);
}
