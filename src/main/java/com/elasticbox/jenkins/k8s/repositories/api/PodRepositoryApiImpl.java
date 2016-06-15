package com.elasticbox.jenkins.k8s.repositories.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.apache.commons.io.IOUtils;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PodRepositoryApiImpl implements PodRepository {

    private static final Logger LOGGER = Logger.getLogger(PodRepositoryApiImpl.class.getName() );

    @Inject
    KubernetesRepository kubeRepository;

    @Override
    public void create(String kubeName, String namespace, Pod pod) throws RepositoryException {
        if (LOGGER.isLoggable(Level.CONFIG) ) {
            LOGGER.config("Creating Pod: " + pod.getMetadata().getName() );
        }
        kubeRepository.getClient(kubeName).pods().inNamespace(namespace).create(pod);
    }

    @Override
    public void create(String kubeName, String namespace, Pod pod, Map<String, String> labels)
            throws RepositoryException {

        if (labels != null) {
            final Map<String, String> currentLabels = pod.getMetadata().getLabels();
            currentLabels.putAll(labels);
            pod.getMetadata().setLabels(currentLabels);
        }

        this.create(kubeName, namespace, pod);
    }

    @Override
    public void delete(String kubeName, String namespace, Pod pod) throws RepositoryException {
        if (LOGGER.isLoggable(Level.CONFIG) ) {
            LOGGER.config("Deleting Pod: " + pod.getMetadata().getName() );
        }
        kubeRepository.getClient(kubeName).pods().inNamespace(namespace).delete(pod);
    }

    @Override
    public void delete(String kubeName, String namespace, String podName) throws RepositoryException {
        if (LOGGER.isLoggable(Level.CONFIG) ) {
            LOGGER.config("Deleting Pod: " + podName );
        }
        kubeRepository.getClient(kubeName).pods().inNamespace(namespace).withName(podName).delete();
    }

    @Override
    public Pod pod(String kubeName, String namespace, String yaml) throws RepositoryException {
        Pod pod;
        try {
            pod = kubeRepository.getClient(kubeName)
                .pods()
                .inNamespace(namespace)
                .load(IOUtils.toInputStream(yaml))
                .get();

            return pod;

        } catch (KubernetesClientException exception) {
            final RepositoryException repoException = new RepositoryException("Error while parsing Yaml", exception);
            LOGGER.warning("Yaml definition not valid: " + repoException.getCausedByMessages());
            throw repoException;
        }
    }

    @Override
    public List<Pod> getAllPods(String kubeName, String deploymentNamespace) throws RepositoryException {

        final KubernetesClient kubernetesClient = kubeRepository.getClient(kubeName);

        PodList list = kubernetesClient.pods().inNamespace(deploymentNamespace).list();

        return list.getItems();
    }

    @Override
    public Pod getPod(String kubeName, String namespace, String podName) throws RepositoryException {

        final KubernetesClient kubernetesClient = kubeRepository.getClient(kubeName);

        final Pod pod = kubernetesClient.pods().inNamespace(namespace).withName(podName).get();

        return pod;
    }

}

