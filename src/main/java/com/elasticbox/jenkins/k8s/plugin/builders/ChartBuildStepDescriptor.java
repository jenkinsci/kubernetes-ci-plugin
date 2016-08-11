/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.plugin.builders;

import com.google.inject.Injector;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.plugin.clouds.ChartRepositoryConfig;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.util.PluginHelper;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;
import java.util.logging.Logger;

public abstract class ChartBuildStepDescriptor extends BuildStepDescriptor<Builder> {

    private static final Logger LOGGER = Logger.getLogger(ChartBuildStepDescriptor.class.getName() );

    private final Injector injector;
    private final String displayName;

    ChartRepository chartRepository;

    KubernetesRepository kubeRepository;

    public ChartBuildStepDescriptor(Class<? extends Builder> clazz, Injector injector, ChartRepository chartRepository,
                                    KubernetesRepository kubeRepository, String displayName) {
        super(clazz);
        this.injector = injector;
        this.chartRepository = chartRepository;
        this.kubeRepository = kubeRepository;
        this.displayName = displayName;
    }

    public ChartBuildStepDescriptor(Class<? extends Builder> clazz, String displayName) {
        this(clazz, null, null, null, displayName);
    }

    public Injector getInjector() {
        return injector;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }


    public ListBoxModel doFillKubeNameItems() {
        List<KubernetesCloud> cloudList = KubernetesCloud.getKubernetesClouds();

        ListBoxModel items = new ListBoxModel(PluginHelper.OPTION_CHOOSE_CLOUD);
        if (cloudList != null && cloudList.size() == 1) {
            final KubernetesCloud cloud = cloudList.get(0);
            items.add(new ListBoxModel.Option(cloud.getDisplayName(), cloud.getName(), true) );

        } else {
            for (KubernetesCloud cloud : cloudList) {
                items.add(cloud.getDisplayName(), cloud.getName() );
            }
        }
        return items;
    }

    public ListBoxModel doFillNamespaceItems(@QueryParameter String kubeName, @QueryParameter String namespace) {
        ListBoxModel items =  new ListBoxModel();

        if (kubeName == null) {
            return items;
        }

        KubernetesCloud kubeCloud = KubernetesCloud.getKubernetesCloud(kubeName);
        if (kubeCloud == null) {
            return items;
        }

        items = PluginHelper.doFillNamespaceItems(kubeRepository.getNamespaces(kubeCloud.getKubernetesCloudParams() ));

        String selectNamespace = (StringUtils.isNotEmpty(namespace) ) ? namespace : kubeCloud.getPredefinedNamespace();

        for (ListBoxModel.Option option: items) {
            if (option.name.equals(selectNamespace) ) {
                option.selected = true;
                break;
            }
        }

        return items;
    }

    public ListBoxModel doFillChartsRepoItems(@QueryParameter String kubeName) {
        ListBoxModel items =  new ListBoxModel(PluginHelper.OPTION_CHOOSE_CHART_REPO_CONFIG);

        if (kubeName == null) {
            return items;
        }

        KubernetesCloud kubeCloud = KubernetesCloud.getKubernetesCloud(kubeName);
        if (kubeCloud == null) {
            return items;
        }

        List<ChartRepositoryConfig> chartRepoConfigList = kubeCloud.getChartRepositoryConfigurations();

        if (chartRepoConfigList != null && chartRepoConfigList.size() == 1) {
            final String description = chartRepoConfigList.get(0).getDescription();
            items.add(new ListBoxModel.Option(description, description, true) );

        } else {
            if (chartRepoConfigList != null) {
                for (ChartRepositoryConfig config : chartRepoConfigList) {
                    items.add(config.getDescription());
                }
            }
        }

        return items;
    }

    public ListBoxModel doFillChartNameItems(@QueryParameter String kubeName, @QueryParameter String chartsRepo) {
        if (kubeName != null && chartsRepo != null) {
            KubernetesCloud kubeCloud = KubernetesCloud.getKubernetesCloud(kubeName);
            if (kubeCloud != null) {
                ChartRepositoryConfig config = kubeCloud.getChartRepositoryConfiguration(chartsRepo);

                if (config != null) {
                    Authentication authData = PluginHelper.getAuthenticationData(config.getCredentialsId());
                    ChartRepo chartRepo = new ChartRepo(config.getChartsRepoUrl(), authData);
                    try {
                        return PluginHelper.doFillChartItems(chartRepository.chartNames(chartRepo));
                    } catch (RepositoryException excep) {
                        LOGGER.severe("Error retrieving chart list from Charts repo: "
                                + chartsRepo + "@" + chartRepo.getUrl());
                    }
                }
            }
        }
        return PluginHelper.doFillChartItems(null);
    }

    public FormValidation doCheckChartName(@QueryParameter String chartName) {
        if (StringUtils.isBlank(chartName)) {
            return FormValidation.error("Chart selection required");
        }
        return FormValidation.ok();
    }
}