package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AbstractPodDeployment;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;

import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfigurationParams;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.model.Label;
import org.apache.commons.lang.ArrayUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class SelectSuitablePodConfiguration extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(SelectSuitablePodConfiguration.class.getName());

    /**
     * Its mission is to create the Pod object that is going to be deployed as Jenkins slave.
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        final Label jobLabel = deploymentContext.getJobLabel();
        if (jobLabel == null) {
            //default option
            deploymentContext.setPodConfigurationChosen(deploymentContext.getAvailablePodConfigurations().get(0));
            return;
        }

        for ( PodSlaveConfigurationParams config: deploymentContext.getAvailablePodConfigurations()) {

            final String [] labels = config.getLabels();
            if (ArrayUtils.contains(labels, jobLabel.getName())) {
                deploymentContext.setPodConfigurationChosen(config);
                return;
            }

        }

        //There is no pod configuration to handle this label
        String message = "There is no Pod slave configuration to handle this label: " + jobLabel.getName();
        LOGGER.log(Level.SEVERE, message);

        throw new ServiceException(message);

    }



}
