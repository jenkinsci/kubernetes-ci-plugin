package com.elasticbox.jenkins.k8s.repositories.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloudParams;
import com.elasticbox.jenkins.k8s.plugin.util.KeyValuePair;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.api.kubeclient.KubernetesClientFactory;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class KubernetesRepositoryApiImpl implements KubernetesRepository {
    private static final Logger LOGGER = Logger.getLogger(KubernetesRepositoryApiImpl.class.getName() );

    @Inject
    KubernetesClientFactory kubeFactory;

    @Override
    public boolean testConnection(KubernetesCloudParams kubeCloudParams) throws RepositoryException {
        try {
            final KubernetesClient kubeClient = kubeFactory.createKubernetesClient(kubeCloudParams);
            return getNamespacesInternal(kubeClient).size() > 0;
        }  catch (KubernetesClientException excep) {
            throw new RepositoryException(excep);
        }
    }

    @Override
    public KubernetesClient getClient(String kubeName) throws RepositoryException {
        return kubeFactory.getKubernetesClient(kubeName);
    }

    public boolean namespaceExists(String kubeName, String namespace) throws RepositoryException {
        return namespaceExistsInternal(getClient(kubeName), namespace);
    }

    private boolean namespaceExistsInternal(KubernetesClient kubeClient, String namespace) throws RepositoryException {
        Namespace existingNamespace = null;

        try {
            existingNamespace = kubeClient.namespaces().withName(namespace).get();
        } catch (KubernetesClientException kce) {
            throw new RepositoryException(kce);
        }
        return existingNamespace != null;
    }

    @Override
    public Namespace createNamespece(String kubeName, String namespace, KeyValuePair<String, String>... labels)
            throws RepositoryException {

        Namespace newNamespace = new NamespaceBuilder().withNewMetadata().withName(namespace).endMetadata().build();
        if (labels.length > 0) {
            Map<String, String> labelsMap = new HashMap<>();
            for (KeyValuePair<String, String> label: Arrays.asList(labels) ) {
                labelsMap.put(label.getKey(), label.getValue() );
            }
            newNamespace.getMetadata().setLabels(labelsMap);
        }

        return  getClient(kubeName).namespaces().create(newNamespace);
    }

    @Override
    public List<String> getNamespaces(String kubeName) {
        try {
            return getNamespacesInternal(getClient(kubeName) );
        } catch (RepositoryException excep) {
            LOGGER.severe("Unable to get client: " + excep.getMessage() );
            return null;
        } catch (KubernetesClientException excep) {
            LOGGER.severe("Unable to get namespaces: " + new RepositoryException(excep).getInitialCause() );
            return null;
        }
    }

    @Override
    public List<String> getNamespaces(KubernetesCloudParams kubeCloudParams) {
        try {
            final KubernetesClient kubeClient = kubeFactory.createKubernetesClient(kubeCloudParams);
            return getNamespacesInternal(kubeClient);
        } catch (KubernetesClientException excep) {
            LOGGER.severe("Unable to get namespaces: " + new RepositoryException(excep).getInitialCause() );
            return null;
        }
    }

    private List<String> getNamespacesInternal(KubernetesClient kubeClient) {
        List<String> returnList = new ArrayList<>();
        for (Namespace namespace: kubeClient.namespaces().list().getItems() ) {
            returnList.add(namespace.getMetadata().getName() );
        }
        return returnList;
    }
}
