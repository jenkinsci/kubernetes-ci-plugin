package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AbstractPodDeployment;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.plugin.slaves.KubernetesSlave;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;

import hudson.model.Descriptor;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class AddSlaveToJenkinsCloud extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(AddSlaveToJenkinsCloud.class.getName());

    private static final String JENKINS_SLAVE_NAME_PREFIX = "jenkins-slave";

    @Inject
    private PodRepository podRepository;

    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        //set the name of the pod and the slave to deploy
        StringBuilder builder =  new StringBuilder(JENKINS_SLAVE_NAME_PREFIX);
        builder.append("-");
        builder.append(Long.toHexString(System.nanoTime()));
        final String podName = builder.toString();

        try {

            KubernetesSlave slave = new KubernetesSlave(podName, podRepository,
                                                        deploymentContext.getCloudToDeployInto(),
                                                        deploymentContext.getJobLabel());

            Jenkins.getInstance().addNode(slave);

            LOGGER.log(Level.INFO, "Added: " + slave.getNodeName() + " to Jenkins cloud");

            deploymentContext.setKubernetesSlave(slave);

        } catch (Descriptor.FormException | IOException exception) {
            LOGGER.log(Level.SEVERE, "Error creating the KubernetesSlave ",exception);
            throw new ServiceException(exception);
        }
    }
}
