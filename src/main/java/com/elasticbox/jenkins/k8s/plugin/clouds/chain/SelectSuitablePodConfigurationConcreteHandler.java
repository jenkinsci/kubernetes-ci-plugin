package com.elasticbox.jenkins.k8s.plugin.clouds.chain;

import com.elasticbox.jenkins.k8s.pod.PodConfiguration;
import hudson.model.Label;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by serna on 5/12/16.
 */
public class SelectSuitablePodConfigurationConcreteHandler extends AbstractPodDeploymentConcreteHandler {

    private static final Logger LOGGER = Logger.getLogger(SelectSuitablePodConfigurationConcreteHandler.class.getName());

    /**
     * Its mission is to create the Pod object that is going to be deployed as Jenkins slave
     * @param deploymentContext
     */
    @Override
    public void handle(JenkinsPodSlaveDeploymentContext deploymentContext) {

        final Label jobLabel = deploymentContext.getJobLabel();
        if (jobLabel == null) {
            //default option
            deploymentContext.setPodConfigurationChosen(deploymentContext.getAvailablePodConfigurations().get(0));
        }

        for( PodConfiguration config: deploymentContext.getAvailablePodConfigurations()) {

            final String labels = config.getLabels();

            final String[] split = StringUtils.split(labels, ' ');
            if (ArrayUtils.contains(split, jobLabel.getName())) {
                deploymentContext.setPodConfigurationChosen(config);
            }

        }

        //There is no pod configuration to handle this label
        String message = "There is no Pod slave configuration to handle this label: " + jobLabel.getName();
        LOGGER.log(Level.SEVERE, message);
        throw new RuntimeException(message);

    }

}
