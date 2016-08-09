/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

import static com.elasticbox.jenkins.k8s.util.Constants.JENKINS_URL;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.plugin.slaves.KubernetesSlave;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AbstractPodDeployment;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfigurationParams;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;

import hudson.model.Label;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;

import jenkins.model.JenkinsLocationConfiguration;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CreatePodFromPodConfiguration extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(CreatePodFromPodConfiguration.class.getName());

    public static final String ELASTICKUBE_COM_JENKINS_LABEL = "elastickube.com/jenkins-label";
    public static final String ELASTICKUBE_COM_JENKINS_SLAVE = "elastickube.com/jenkins-slave";


    @Inject
    private PodRepository podRepository;

    /**
     * Its mission is to check if we can provision one slave more. It will be impossible if we have already reached
     * the specific limit for the cloud
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        final KubernetesCloud cloudToDeployInto = deploymentContext.getCloudToDeployInto();

        final PodSlaveConfigurationParams podConfigurationChosen = deploymentContext.getPodConfigurationChosen();

        if (podConfigurationChosen == null) {
            throw new ServiceException("No pod configuration available to handle label: "
                    + deploymentContext.getJobLabel() );
        }

        try {

            final Pod podToDeploy = podRepository.pod(cloudToDeployInto.getName(),
                                                        cloudToDeployInto.getPredefinedNamespace(),
                                                        podConfigurationChosen.getPodYaml());

            addName(podToDeploy, deploymentContext);
            addRestartPolicy(podToDeploy, deploymentContext);
            addLabels(podToDeploy, deploymentContext);
            addEnvironmentVariables(podToDeploy, deploymentContext);

            deploymentContext.setPodToDeploy(podToDeploy);

        } catch (RepositoryException exception) {

            LOGGER.log(Level.SEVERE, "Error creating Pod model object: " + podConfigurationChosen);
            throw new ServiceException("Error creating Pod model object", exception);
        }
    }

    public void addName(Pod podToDeploy, PodDeploymentContext deploymentContext) {

        final String nodeName = deploymentContext.getKubernetesSlave().getNodeName();
        podToDeploy.getMetadata().setName(nodeName);

        LOGGER.info("Pod object model created from PodConfiguration with name: " + nodeName);
    }

    public void addRestartPolicy(Pod podToDeploy, PodDeploymentContext deploymentContext) {

        //set the restart policy in order to not restart if something goes wrong
        podToDeploy.getSpec().setRestartPolicy("Never");
    }

    public void addEnvironmentVariables(Pod podToDeploy, PodDeploymentContext deploymentContext) {

        final KubernetesSlave kubernetesSlave = deploymentContext.getKubernetesSlave();

        for (Container container: podToDeploy.getSpec().getContainers()) {

            List<EnvVar> currentEnv = container.getEnv();
            boolean found = false;
            for (EnvVar var: currentEnv) {
                if (var.getName().equals(JENKINS_URL) ) {
                    found = true;
                    break;
                }
            }
            if ( !found) {
                String url = JenkinsLocationConfiguration.get().getUrl();
                currentEnv.add(new EnvVar(JENKINS_URL, url, null));
            }

            container.setWorkingDir(KubernetesSlave.DEFAULT_REMOTE_FS);

            container.getArgs().add(kubernetesSlave.getComputer().getJnlpMac());
            container.getArgs().add(kubernetesSlave.getComputer().getName());

            LOGGER.log(Level.INFO, "Added environment variables to container: " + container.getName() );
        }

    }

    public void addLabels(Pod podToDeploy, PodDeploymentContext deploymentContext) {

        Map<String, String> podLabels = podToDeploy.getMetadata().getLabels();

        final Label jobLabel = deploymentContext.getJobLabel();
        if (jobLabel != null) {
            podLabels.put(ELASTICKUBE_COM_JENKINS_LABEL, jobLabel.getName() );

            LOGGER.info("JobLabel [" + ELASTICKUBE_COM_JENKINS_LABEL + " = " + jobLabel.getName() + "] added to Pod ");
        }

        final String podName = podToDeploy.getMetadata().getName();
        podLabels.put(ELASTICKUBE_COM_JENKINS_SLAVE, podName);

        LOGGER.info("Label [" + ELASTICKUBE_COM_JENKINS_SLAVE + " = " + podName + "] added to Pod ");

        podToDeploy.getMetadata().setLabels(podLabels);
    }
}

