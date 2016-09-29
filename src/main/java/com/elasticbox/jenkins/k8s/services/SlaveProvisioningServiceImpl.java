/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.services;

import com.google.inject.Inject;

import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps.SelectSuitablePodConfiguration;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.SlaveProvisioningStep;
import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfigurationParams;
import com.elasticbox.jenkins.k8s.plugin.slaves.KubernetesSlave;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;

import hudson.Extension;
import hudson.model.Label;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class SlaveProvisioningServiceImpl implements SlaveProvisioningService {

    private static final Logger LOGGER = Logger.getLogger(SlaveProvisioningServiceImpl.class.getName());

    @Inject
    private Set<SlaveProvisioningStep> podCreationChainHandlers;

    @Inject
    private SelectSuitablePodConfiguration selectSuitablePodConfiguration;


    public KubernetesSlave slaveProvision(KubernetesCloud kubernetesCloud,
                                          List<PodSlaveConfigurationParams> podConfigurations,
                                          Label label) throws ServiceException {

        PodDeploymentContext deploymentContext =
            new PodDeploymentContext.JenkinsPodSlaveDeploymentContextBuilder()
                .withJobLabel(label)
                .withOneOfThesePodConfigurations(podConfigurations)
                .intoKubernetesCloud(kubernetesCloud)
                .withNamespace(kubernetesCloud.getPredefinedNamespace() )
                .build();

        try {
            for (SlaveProvisioningStep deploymentHandler: podCreationChainHandlers) {
                deploymentHandler.handle(deploymentContext);
            }

            final KubernetesSlave kubernetesSlave = deploymentContext.getKubernetesSlave();
            LOGGER.log(Level.INFO, "Provision done. The pod is running and the slave is online: " + kubernetesSlave);
            return kubernetesSlave;

        } catch (ServiceException exception) {
            LOGGER.severe("Error provisioning Pod Jenkins slave ");
            throw exception;
        }
    }

    @Override
    public boolean canProvision(KubernetesCloud kubernetesCloud,
                                List<PodSlaveConfigurationParams> podConfigurations,
                                Label label) throws ServiceException {

        PodDeploymentContext deploymentContext =
            new PodDeploymentContext.JenkinsPodSlaveDeploymentContextBuilder()
                .withJobLabel(label)
                .withOneOfThesePodConfigurations(podConfigurations)
                .intoKubernetesCloud(kubernetesCloud)
                .withNamespace(kubernetesCloud.getPredefinedNamespace() )
                .build();

        this.selectSuitablePodConfiguration.handle(deploymentContext);

        return deploymentContext.getPodConfigurationChosen() != null;
    }
}
