package com.elasticbox.jenkins.k8s.plugin.clouds;

public class PodSlaveConfigurationParams {
    final String description;
    final String podYaml;
    final String label;

    public PodSlaveConfigurationParams(String description, String podYaml, String label) {
        this.description = description;
        this.podYaml = podYaml;
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public String getPodYaml() {
        return podYaml;
    }

    @Override
    public String toString() {
        return "PodSlaveConfigurationParams{description='" + description + '\'' + ", label='" + label + '\'' + '}';
    }
}