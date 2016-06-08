package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AbstractPodDeployment;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;

import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfigurationParams;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.model.Label;
import org.apache.commons.lang.ArrayUtils;

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
            PodSlaveConfigurationParams podConfiguration = deploymentContext.getAvailablePodConfigurations().get(0);
            LOGGER.config("No label provided, returning first available pod configuration: " + podConfiguration);
            deploymentContext.setPodConfigurationChosen(podConfiguration);
            return;
        }

        for ( PodSlaveConfigurationParams config: deploymentContext.getAvailablePodConfigurations()) {
            LOGGER.config("Looking for a slave configuration with label: " + jobLabel);

            final String [] labels = config.getLabels();
            if (ArrayUtils.contains(labels, jobLabel.getName())) {
                deploymentContext.setPodConfigurationChosen(config);
                return;
            }

        }

        //There is no pod configuration to handle this label
        LOGGER.config("There is no Pod slave configuration to handle this label: " + jobLabel.getName() );
    }
}
