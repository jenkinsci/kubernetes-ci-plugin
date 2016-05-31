package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain;

import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.plugin.slaves.KubernetesSlave;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class AddEnvironmentVariablesToPodConfiguration extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(AddEnvironmentVariablesToPodConfiguration.class.getName());


    private KubernetesRepository kubernetesRepository;

    /**
     * Its mission is to add values for the environment variables , if needed.
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        final KubernetesSlave slave = deploymentContext.getKubernetesSlave();
        final String jenkinsUrl = JenkinsLocationConfiguration.get().getUrl();

        List<EnvVar> env = new ArrayList<EnvVar>(3);
        env.add(new EnvVar("JENKINS_SECRET", slave.getComputer().getJnlpMac(), null));
        env.add(new EnvVar("JENKINS_LOCATION_URL", JenkinsLocationConfiguration.get().getUrl(), null));
        String url = StringUtils.isBlank(jenkinsUrl) ? JenkinsLocationConfiguration.get().getUrl() : jenkinsUrl;
        env.add(new EnvVar("JENKINS_URL", url, null));
        url = url.endsWith("/") ? url : url + "/";
        env.add(new EnvVar("JENKINS_JNLP_URL", url + slave.getComputer().getUrl() + "slave-agent.jnlp", null));
        env.add(new EnvVar("HOME", KubernetesSlave.DEFAULT_REMOTE_FS, null));

        final Pod podToDeploy = deploymentContext.getPodToDeploy();
        podToDeploy.getSpec().setRestartPolicy("Never");

        for (Container container: podToDeploy.getSpec().getContainers()) {
            container.setEnv(env);
            container.setWorkingDir(KubernetesSlave.DEFAULT_REMOTE_FS);
            container.getArgs().add(slave.getComputer().getJnlpMac());
            container.getArgs().add(slave.getComputer().getName());

            LOGGER.log(Level.INFO, "Added environment variables to container {0}", container.getName());
        }


    }


}
