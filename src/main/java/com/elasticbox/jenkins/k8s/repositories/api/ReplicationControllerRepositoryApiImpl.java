/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.repositories.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.ReplicationControllerRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Map;
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
    public void create(String kubeName, String namespace, ReplicationController controller, Map<String, String> labels)
            throws RepositoryException {

        if (labels != null) {
            final Map<String, String> currentLabels = controller.getMetadata().getLabels();
            currentLabels.putAll(labels);
            controller.getMetadata().setLabels(currentLabels);
        }

        this.create(kubeName, namespace, controller);
    }
    
    @Override
    public void delete(String kubeName, String namespace, ReplicationController replController)
            throws RepositoryException {

        String replControllerName = replController.getMetadata().getName();

        if (LOGGER.isLoggable(Level.CONFIG) ) {
            LOGGER.config("Deleting Replication Controller and associated Pods: " + replControllerName);
        }

        final KubernetesClient client = kubeRepository.getClient(kubeName);

        client.replicationControllers().inNamespace(namespace).withName(replControllerName).scale(0, true);

        client.replicationControllers().inNamespace(namespace).delete(replController);
    }
}
