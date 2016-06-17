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
import com.elasticbox.jenkins.k8s.repositories.ServiceRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Service;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ServiceRepositoryApiImpl implements ServiceRepository {
    private static final Logger LOGGER = Logger.getLogger(ServiceRepositoryApiImpl.class.getName() );

    @Inject
    KubernetesRepository kubeRepository;

    @Override
    public void create(String kubeName, String namespace, Service service) throws RepositoryException {
        if (LOGGER.isLoggable(Level.CONFIG) ) {
            LOGGER.config("Creating Service: " + service.getMetadata().getName() );
        }
        kubeRepository.getClient(kubeName).services().inNamespace(namespace).create(service);
    }

    @Override
    public void create(String kubeName, String namespace, Service service, Map<String, String> labels)
            throws RepositoryException {

        if (labels != null) {
            final Map<String, String> currentLabels = service.getMetadata().getLabels();
            currentLabels.putAll(labels);
            service.getMetadata().setLabels(currentLabels);
        }

        this.create(kubeName, namespace, service);
    }

    @Override
    public void delete(String kubeName, String namespace, Service service) throws RepositoryException {
        if (LOGGER.isLoggable(Level.CONFIG) ) {
            LOGGER.config("Deleting Service: " + service.getMetadata().getName() );
        }
        kubeRepository.getClient(kubeName).services().inNamespace(namespace).delete(service);
    }
}
