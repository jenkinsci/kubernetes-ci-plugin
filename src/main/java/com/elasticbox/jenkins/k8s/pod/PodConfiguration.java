package com.elasticbox.jenkins.k8s.pod;

/**
 * Created by serna on 5/17/16.
 */
public class PodConfiguration {

    private String yamlPod;
    private String description;
    private String labels;

    public PodConfiguration(String yamlPod, String description, String labels) {
        this.yamlPod = yamlPod;
        this.description = description;
        this.labels = labels;
    }

    public String getYamlPod() {
        return yamlPod;
    }

    public String getDescription() {
        return description;
    }

    public String getLabels() {
        return labels;
    }

    public void setYamlPod(String yamlPod) {
        this.yamlPod = yamlPod;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }
}
