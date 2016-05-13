package com.elasticbox.jenkins.k8s.plugin.builders;

import com.google.inject.Inject;
import com.google.inject.Injector;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.plugin.clouds.ChartRepositoryConfig;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.util.PluginHelper;
import com.elasticbox.jenkins.k8s.plugin.util.TaskLogger;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.ChartDeploymentService;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public abstract class BaseChartBuildStep extends Builder implements SimpleBuildStep {
    private static final Logger LOGGER = Logger.getLogger(BaseChartBuildStep.class.getName() );

    protected String id;
    protected String cloudName;
    protected String chartsRepo;
    protected String chartName;

    @Inject
    transient ChartDeploymentService deploymentService;

    protected Object readResolve() {
        injectMembers();
        return this;
    }

    protected void injectMembers() {
        ((DescriptorImpl) getDescriptor()).injector.injectMembers(this);
    }

    public String getId() {
        return id;
    }

    public String getCloudName() {
        return cloudName;
    }

    public String getChartsRepo() {
        return chartsRepo;
    }

    public String getChartName() {
        return chartName;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws InterruptedException, IOException {

        TaskLogger taskLogger = new TaskLogger(taskListener, LOGGER);
        taskLogger.info("Executing Chart build step: " + run);

        try {
            KubernetesCloud kubeCloud = KubernetesCloud.getKubernetesCloud(getCloudName() );
            taskLogger.info("Using Kubernetes clouds config: " + kubeCloud);

            ChartRepositoryConfig config = kubeCloud.getChartRepositoryConfiguration(getChartsRepo() );
            taskLogger.info("Using Chart repository config: " + config);

            Authentication authData = PluginHelper.getAuthenticationData(config.getCredentialsId() );
            ChartRepo chartRepo = new ChartRepo(config.getChartsRepoUrl(), authData);

            doPerform(taskLogger, kubeCloud, chartRepo);

        } catch (ServiceException exception) {
            taskLogger.error(exception.getCausedByMessages() );
            throw new IOException(exception);

        } catch (NullPointerException exception) {
            final String message = "Cannot initialize resources: " + exception.getMessage();
            taskLogger.error(message);
            throw new IOException(message, exception);
        }
    }

    protected abstract void doPerform(TaskLogger taskLogger, KubernetesCloud kubeCloud, ChartRepo chartRepo)
            throws ServiceException;

    protected static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Inject
        private Injector injector;

        @Inject
        ChartRepository chartRepository;

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return false;
        }

        public Injector getInjector() {
            return injector;
        }

        public ListBoxModel doFillCloudNameItems() {
            ListBoxModel items = new ListBoxModel(PluginHelper.OPTION_CHOOSE_CLOUD);
            List<KubernetesCloud> cloudList = KubernetesCloud.getKubernetesClouds();

            for (KubernetesCloud cloud : cloudList) {
                items.add(cloud.getDisplayName(), cloud.name);
            }
            return items;
        }

        public ListBoxModel doFillChartsRepoItems(@QueryParameter String cloudName) {
            ListBoxModel items = new ListBoxModel(PluginHelper.OPTION_CHOOSE_CHART_REPO_CONFIG);
            if (cloudName != null) {
                KubernetesCloud kubeCloud = KubernetesCloud.getKubernetesCloud(cloudName);
                if (kubeCloud != null) {
                    List<ChartRepositoryConfig> chartRepoConfigList = kubeCloud.getChartRepositoryConfigurations();

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

        public FormValidation doCheckNamespace(@QueryParameter String namespace) {
            if (StringUtils.isBlank(namespace)) {
                return FormValidation.error("Namespace is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckChartName(@QueryParameter String chartName) {
            if (StringUtils.isBlank(chartName)) {
                return FormValidation.error("Chart selection required");
            }
            return FormValidation.ok();
        }
    }
}
