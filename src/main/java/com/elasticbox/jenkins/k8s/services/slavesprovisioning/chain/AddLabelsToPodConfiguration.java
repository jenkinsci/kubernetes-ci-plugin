package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.model.Label;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


@Singleton
public class AddLabelsToPodConfiguration extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(AddLabelsToPodConfiguration.class.getName());

    public static final String ELASTICKUBE_COM_JENKINS_LABEL = "elastickube.com/jenkins-label";
    public static final String ELASTICKUBE_COM_JENKINS_SLAVE = "elastickube.com/jenkins-slave";

    /**
     * Its mission is to add the labels to the Pod if needed.
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        final Pod podToDeploy = deploymentContext.getPodToDeploy();
        Map<String, String> podLabels = podToDeploy.getMetadata().getLabels();

        final Label jobLabel = deploymentContext.getJobLabel();
        if (jobLabel != null) {
            podLabels.put(ELASTICKUBE_COM_JENKINS_LABEL, jobLabel.getName());

            LOGGER.log(Level.INFO,
                "Label [" + ELASTICKUBE_COM_JENKINS_LABEL + "/" + jobLabel.getName() + "] added to Pod ");
        }

        final String podName = podToDeploy.getMetadata().getName();
        podLabels.put(ELASTICKUBE_COM_JENKINS_SLAVE, podName);

        LOGGER.log(Level.INFO,
            "Label [" + ELASTICKUBE_COM_JENKINS_SLAVE + "/" + podName + "] added to Pod ");

        podToDeploy.getMetadata().setLabels(podLabels);

    }



}
