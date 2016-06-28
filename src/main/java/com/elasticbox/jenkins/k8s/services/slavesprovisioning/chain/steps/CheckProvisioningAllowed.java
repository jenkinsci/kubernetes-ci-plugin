/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AbstractPodDeployment;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CheckProvisioningAllowed extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(CheckProvisioningAllowed.class.getName());

    @Inject
    private PodRepository podRepository;


    /**
     * Its mission is to check if we can provision one slave more. It will be impossible if we have already reached
     * the specific limit for the cloud
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        final KubernetesCloud cloudToDeployInto = deploymentContext.getCloudToDeployInto();

        final int cloudCapacity = cloudToDeployInto.getInstanceCap();

        if (cloudCapacity == 0) {
            //is not possible to deploy any slave in this kubernetes cloud
            String message = "It is impossible to deploy a slave in a cloud which capacity is zero";
            LOGGER.log(Level.SEVERE, message);
            throw new RuntimeException(message);
        }

        String deploymentNamespace = StringUtils.isNotBlank(deploymentContext.getDeploymentNamespace())
            ? deploymentContext.getDeploymentNamespace()
            : cloudToDeployInto.getNamespace();

        try {

            final List<Pod> pods = podRepository.getAllPods(cloudToDeployInto.getName(), deploymentNamespace);

            if (pods.size() >= cloudCapacity) {
                String message = "Not provisioning, max cloud capacity: " + cloudCapacity + "reached";
                LOGGER.log(Level.SEVERE, message);
                throw new ServiceException(message);
            }

            LOGGER.log(Level.INFO, "Pod deployment granted, cloud capacity: " + pods.size() );

        } catch (RepositoryException e) {

            LOGGER.log(Level.SEVERE, "Error getting the kubernetes client for the cloud "
                    + cloudToDeployInto.getName() );

            throw new ServiceException("Error getting the kubernetes client for the cloud "
                    + cloudToDeployInto.getName() );
        }
    }
}
