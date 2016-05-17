package com.elasticbox.jenkins.k8s.plugin.builders;

import com.google.inject.Injector;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.plugin.clouds.ChartRepositoryConfig;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.util.PluginHelper;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
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


    public ChartBuildStepDescriptor(Class<? extends Builder> clazz, Injector injector,
                                    ChartRepository chartRepository, String displayName) {
        super(clazz);
        this.injector = injector;
        this.chartRepository = chartRepository;
        this.displayName = displayName;
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


    public ListBoxModel doFillCloudNameItems() {
        List<KubernetesCloud> cloudList = KubernetesCloud.getKubernetesClouds();

        ListBoxModel items = new ListBoxModel(PluginHelper.OPTION_CHOOSE_CLOUD);
        if (cloudList != null && cloudList.size() == 1) {
            final KubernetesCloud cloud = cloudList.get(0);
            items.add(new ListBoxModel.Option(cloud.getDisplayName(), cloud.name, true) );

        } else {
            for (KubernetesCloud cloud : cloudList) {
                items.add(cloud.getDisplayName(), cloud.name);
            }
        }
        return items;
    }

    public ListBoxModel doFillChartsRepoItems(@QueryParameter String cloudName) {
        ListBoxModel items =  new ListBoxModel(PluginHelper.OPTION_CHOOSE_CHART_REPO_CONFIG);

        if (cloudName == null) {
            return items;
        }

        KubernetesCloud kubeCloud = KubernetesCloud.getKubernetesCloud(cloudName);
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

    public ListBoxModel doFillChartNameItems(@QueryParameter String cloudName, @QueryParameter String chartsRepo) {
        if (cloudName != null && chartsRepo != null) {
            KubernetesCloud kubeCloud = KubernetesCloud.getKubernetesCloud(cloudName);
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