package com.elasticbox.jenkins.k8s.plugin.clouds.chain;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.clouds.slaves.JenkinsKubernetesSlave;
import com.elasticbox.jenkins.k8s.pod.PodConfiguration;
import hudson.model.Label;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.List;

/**
 * Created by serna on 5/12/16.
 */
public class JenkinsPodSlaveDeploymentContext {

    private Label jobLabel;
    private List<PodConfiguration> availablePodConfigurations;

    private Pod podToDeploy;
    private PodConfiguration podConfigurationChosen;

    private KubernetesCloud cloudToDeployInto;
    private String deploymentNamespace;

    private JenkinsKubernetesSlave jenkinsKubernetesSlave;

    private JenkinsPodSlaveDeploymentContext(JenkinsPodSlaveDeploymentContextBuilder builder) {
        jobLabel = builder.jobLabel;
        availablePodConfigurations = builder.podConfigurations;
        cloudToDeployInto = builder.kubernetesCloud;
        deploymentNamespace = builder.deploymentNamespace;
    }

    public PodConfiguration getPodConfigurationChosen() {
        return podConfigurationChosen;
    }

    public String getDeploymentNamespace() {
        return deploymentNamespace;
    }

    public Label getJobLabel() {
        return jobLabel;
    }

    public KubernetesCloud getCloudToDeployInto() {
        return cloudToDeployInto;
    }

    public Pod getPodToDeploy() {
        return podToDeploy;
    }

    public List<PodConfiguration> getAvailablePodConfigurations() {
        return availablePodConfigurations;
    }

    public void setPodToDeploy(Pod podToDeploy) {
        this.podToDeploy = podToDeploy;
    }

    public void setPodConfigurationChosen(PodConfiguration podConfigurationChosen) {
        this.podConfigurationChosen = podConfigurationChosen;
    }

    public JenkinsKubernetesSlave getJenkinsKubernetesSlave() {
        return jenkinsKubernetesSlave;
    }

    public void setJenkinsKubernetesSlave(JenkinsKubernetesSlave jenkinsKubernetesSlave) {
        this.jenkinsKubernetesSlave = jenkinsKubernetesSlave;
    }

    public static class JenkinsPodSlaveDeploymentContextBuilder{

        private Label jobLabel;
        private List<PodConfiguration> podConfigurations;
        private KubernetesCloud kubernetesCloud;
        private String deploymentNamespace;

        public JenkinsPodSlaveDeploymentContextBuilder withJobLabel(Label label){
            this.jobLabel = label;
            return this;
        }

        public JenkinsPodSlaveDeploymentContextBuilder withOneOfThesePodConfigurations(
                        List<PodConfiguration> podConfigurations){

            this.podConfigurations = podConfigurations;
            return this;
        }

        public JenkinsPodSlaveDeploymentContextBuilder intoKubernetesCloud(KubernetesCloud kubernetesCloud){
            this.kubernetesCloud = kubernetesCloud;
            return this;
        }

        public JenkinsPodSlaveDeploymentContextBuilder withNamespace(String deploymentNamespace){
            this.deploymentNamespace = deploymentNamespace;
            return this;
        }

        public JenkinsPodSlaveDeploymentContext build() {
            return new JenkinsPodSlaveDeploymentContext(this);
        }

    }

}
