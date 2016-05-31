package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfigurationParams;
import com.elasticbox.jenkins.k8s.plugin.slaves.KubernetesSlave;
import hudson.model.Label;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.List;

public class PodDeploymentContext {

    private Label jobLabel;

    private List<PodSlaveConfigurationParams> availablePodConfigurations;

    private Pod podToDeploy;
    private PodSlaveConfigurationParams podConfigurationChosen;

    private KubernetesCloud cloudToDeployInto;
    private String deploymentNamespace;

    private KubernetesSlave kubernetesSlave;

    private PodDeploymentContext(JenkinsPodSlaveDeploymentContextBuilder builder) {
        jobLabel = builder.jobLabel;
        availablePodConfigurations = builder.podConfigurations;
        cloudToDeployInto = builder.kubernetesCloud;
        deploymentNamespace = builder.deploymentNamespace;
    }

    public PodSlaveConfigurationParams getPodConfigurationChosen() {
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

    public List<PodSlaveConfigurationParams> getAvailablePodConfigurations() {
        return availablePodConfigurations;
    }

    public void setPodToDeploy(Pod podToDeploy) {
        this.podToDeploy = podToDeploy;
    }

    public void setPodConfigurationChosen(PodSlaveConfigurationParams podConfigurationChosen) {
        this.podConfigurationChosen = podConfigurationChosen;
    }

    public KubernetesSlave getKubernetesSlave() {
        return kubernetesSlave;
    }

    public void setKubernetesSlave(KubernetesSlave kubernetesSlave) {
        this.kubernetesSlave = kubernetesSlave;
    }

    public static class JenkinsPodSlaveDeploymentContextBuilder {

        private Label jobLabel;
        private List<PodSlaveConfigurationParams> podConfigurations;
        private KubernetesCloud kubernetesCloud;
        private String deploymentNamespace;

        public JenkinsPodSlaveDeploymentContextBuilder withJobLabel(Label label) {
            this.jobLabel = label;
            return this;
        }

        public JenkinsPodSlaveDeploymentContextBuilder withOneOfThesePodConfigurations(
                        List<PodSlaveConfigurationParams> podConfigurations) {

            this.podConfigurations = podConfigurations;
            return this;
        }

        public JenkinsPodSlaveDeploymentContextBuilder intoKubernetesCloud(KubernetesCloud kubernetesCloud) {
            this.kubernetesCloud = kubernetesCloud;
            return this;
        }

        public JenkinsPodSlaveDeploymentContextBuilder withNamespace(String deploymentNamespace) {
            this.deploymentNamespace = deploymentNamespace;
            return this;
        }

        public PodDeploymentContext build() {
            return new PodDeploymentContext(this);
        }

    }

}
