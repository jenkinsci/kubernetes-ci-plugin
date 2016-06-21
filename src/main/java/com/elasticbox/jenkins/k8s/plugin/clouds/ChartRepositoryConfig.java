/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.plugin.clouds;

import com.google.inject.Inject;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.util.PluginHelper;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;
import java.util.logging.Logger;

public class ChartRepositoryConfig implements Describable<ChartRepositoryConfig> {
    private static final Logger LOGGER = Logger.getLogger(ChartRepositoryConfig.class.getName());

    private final String description;
    private final String chartsRepoUrl;
    private final String credentialsId;

    @DataBoundConstructor
    public ChartRepositoryConfig(String description, String chartsRepoUrl, String credentialsId) {
        this.description = description;
        this.chartsRepoUrl = chartsRepoUrl;
        this.credentialsId = credentialsId;
    }

    public String getDescription() {
        return description;
    }

    public String getChartsRepoUrl() {
        return chartsRepoUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Override
    public Descriptor<ChartRepositoryConfig> getDescriptor() {
        final Jenkins instance = Jenkins.getInstance();
        return (instance != null) ? instance.getDescriptor(getClass() ) : null;
    }

    @Override
    public String toString() {
        return "ChartRepositoryConfig [" + getDescription() + "@" + getChartsRepoUrl() + "]";
    }


    @Extension
    public static class DescriptorImpl extends Descriptor<ChartRepositoryConfig> {

        private static final String CHART_REPOSITORY_CONFIGURATION = "Chart Repository Configuration";

        @Inject
        private transient ChartRepository chartRepository;

        @Override
        public String getDisplayName() {
            return CHART_REPOSITORY_CONFIGURATION;
        }

        public FormValidation doTestConnection(@QueryParameter String chartsRepoUrl,
                                               @QueryParameter String credentialsId) {

            if (StringUtils.isEmpty(chartsRepoUrl)) {
                return FormValidation.error("Required fields not provided");
            }

            Authentication authData = PluginHelper.getAuthenticationData(credentialsId);
            ChartRepo chartRepoData = new ChartRepo(chartsRepoUrl, authData);
            try {
                final List<String> chartNames = chartRepository.chartNames(chartRepoData);
                if (chartNames != null && !chartNames.isEmpty() ) {
                    return FormValidation.ok("Connection successful");
                } else {
                    return FormValidation.error("Invalid repository");
                }
            } catch (RepositoryException excep) {
                String msg = "Connection error - " + excep.getCausedByMessages();
                LOGGER.severe(msg);
                return FormValidation.error(msg);
            }
        }

        public ListBoxModel doFillCredentialsIdItems(@QueryParameter String endpointUrl) {
            return PluginHelper.doFillCredentialsIdItems(endpointUrl);
        }
    }

}
