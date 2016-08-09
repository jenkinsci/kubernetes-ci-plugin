/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.plugin.clouds;

import com.google.inject.Inject;

import com.elasticbox.jenkins.k8s.util.PluginHelper;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.Extension;
import hudson.RelativePath;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.fabric8.kubernetes.api.model.Pod;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PodSlaveConfig implements Describable<PodSlaveConfig> {

    private static final Logger LOGGER = Logger.getLogger(PodSlaveConfig.class.getName());

    private final String id;
    private final PodSlaveConfigurationParams podSlaveConfigurationParams;

    @DataBoundConstructor
    public PodSlaveConfig(String id, String description, String podYaml, String labels) {
        this.id = id;
        this.podSlaveConfigurationParams = new PodSlaveConfigurationParams(description, podYaml, labels);
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return podSlaveConfigurationParams.getDescription();
    }

    public String getPodYaml() {
        return podSlaveConfigurationParams.getPodYaml();
    }

    public PodSlaveConfigurationParams getPodSlaveConfigurationParams() {
        return podSlaveConfigurationParams;
    }

    public String getLabels() {
        return podSlaveConfigurationParams.getLabelsAsString();
    }

    @Override
    public Descriptor<PodSlaveConfig> getDescriptor() {
        final Jenkins instance = Jenkins.getInstance();
        return (instance != null) ? instance.getDescriptor(getClass() ) : null;
    }

    @Override
    public String toString() {
        return podSlaveConfigurationParams.toString();
    }


    @Extension
    public static class DescriptorImpl extends Descriptor<PodSlaveConfig> {

        private static final String POD_SLAVE_CONFIGURATION = "Pod Slave Configuration";

        @Inject
        transient PodRepository podRepository;

        @Override
        public String getDisplayName() {
            return POD_SLAVE_CONFIGURATION;
        }

        public FormValidation doTestYaml(@QueryParameter String podYaml) {

            if (PluginHelper.anyOfThemIsBlank(podYaml) ) {
                return FormValidation.error("Required fields not provided");
            }

            try {
                final Pod pod = podRepository.pod(null, null, podYaml);
                if (LOGGER.isLoggable(Level.FINER) ) {
                    LOGGER.finer("Pod: " + pod);
                }

                return FormValidation.ok("Validation successful");

            } catch (RepositoryException exception) {
                return FormValidation.error("Validation error - " + exception.getCausedByMessages());
            }
        }

        public ListBoxModel doFillCredentialsIdItems(@QueryParameter String endpointUrl) {
            return PluginHelper.doFillCredentialsIdItems(endpointUrl);
        }
    }

}
