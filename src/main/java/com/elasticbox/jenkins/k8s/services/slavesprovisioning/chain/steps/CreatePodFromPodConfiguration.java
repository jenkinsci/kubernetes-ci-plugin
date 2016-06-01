package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

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

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
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

        final KubernetesSlave kubernetesSlave = deploymentContext.getKubernetesSlave();

        final PodSlaveConfigurationParams podConfigurationChosen = deploymentContext.getPodConfigurationChosen();

        try {

            final Pod podToDeploy = podRepository.pod(cloudToDeployInto.name,
                                                        cloudToDeployInto.getNamespace(),
                                                        podConfigurationChosen.getPodYaml());

            addName(podToDeploy, deploymentContext);
            addRestartPolicy(podToDeploy, deploymentContext);
            addLabels(podToDeploy, deploymentContext);
            addEnvironmentVariables(podToDeploy, deploymentContext);

            deploymentContext.setPodToDeploy(podToDeploy);


        } catch (RepositoryException exception) {

            LOGGER.log(Level.SEVERE, "Error creating Pod model object");

            throw new ServiceException("Error creating Pod model object", exception);
        }

    }

    public void addName(Pod podToDeploy, PodDeploymentContext deploymentContext) {

        final String nodeName = deploymentContext.getKubernetesSlave().getNodeName();

        podToDeploy.getMetadata().setName(nodeName);

        LOGGER.log(Level.INFO, "Pod object model created from PodConfiguration with name: " + nodeName);

    }

    public void addRestartPolicy(Pod podToDeploy, PodDeploymentContext deploymentContext) {

        //set the restart policy in order to not restart if something goes wrong
        podToDeploy.getSpec().setRestartPolicy("Never");

    }

    public void addEnvironmentVariables(Pod podToDeploy, PodDeploymentContext deploymentContext) {

        final KubernetesSlave kubernetesSlave = deploymentContext.getKubernetesSlave();

        final String jenkinsUrl = JenkinsLocationConfiguration.get().getUrl();

        List<EnvVar> env = new ArrayList<>();
        env.add(new EnvVar("JENKINS_SECRET", kubernetesSlave.getComputer().getJnlpMac(), null));
        env.add(new EnvVar("JENKINS_LOCATION_URL", JenkinsLocationConfiguration.get().getUrl(), null));

        String url = StringUtils.isBlank(jenkinsUrl) ? JenkinsLocationConfiguration.get().getUrl() : jenkinsUrl;
        env.add(new EnvVar("JENKINS_URL", url, null));

        url = url.endsWith("/") ? url : url + "/";
        env.add(
            new EnvVar(
                    "JENKINS_JNLP_URL",
                    url + kubernetesSlave.getComputer().getUrl() + "slave-agent.jnlp", null));

        env.add(new EnvVar("HOME", KubernetesSlave.DEFAULT_REMOTE_FS, null));


        for (Container container: podToDeploy.getSpec().getContainers()) {
            container.setEnv(env);
            container.setWorkingDir(KubernetesSlave.DEFAULT_REMOTE_FS);
            container.getArgs().add(kubernetesSlave.getComputer().getJnlpMac());
            container.getArgs().add(kubernetesSlave.getComputer().getName());

            LOGGER.log(Level.INFO, "Added environment variables to container {0}", container.getName());
        }

    }

    public void addLabels(Pod podToDeploy, PodDeploymentContext deploymentContext) {

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

