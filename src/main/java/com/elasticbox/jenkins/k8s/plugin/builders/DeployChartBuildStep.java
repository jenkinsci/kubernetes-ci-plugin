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
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.ChartDeploymentService;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.slaves.Cloud;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class DeployChartBuildStep extends Builder implements SimpleBuildStep {
    private static final Logger LOGGER = Logger.getLogger(DeployChartBuildStep.class.getName() );
    private static final String NAME_PREFIX = "DeployChartBS-";

    private final String id;
    private final String cloudName;
    private final String chartsRepo;
    private final String chartName;

    @Inject
    private transient ChartDeploymentService deploymentService;

    @DataBoundConstructor
    public DeployChartBuildStep(String id, String cloudName, String chartsRepo, String chartName) {
        super();
        this.id = StringUtils.isNotEmpty(id)  ? id : NAME_PREFIX + UUID.randomUUID().toString();
        this.cloudName = cloudName;
        this.chartsRepo = chartsRepo;
        this.chartName = chartName;
        ( (DescriptorImpl)getDescriptor() ).injector.injectMembers(this);
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws InterruptedException, IOException {

        TaskLogger taskLogger = new TaskLogger(taskListener, LOGGER);
        taskLogger.info("Executing Deploy Chart build step: " + run);

        try {
            PluginHelper.ensureIsInitialized(this, deploymentService);

            KubernetesCloud kubeCloud = KubernetesCloud.getKubernetesCloud(cloudName);
            taskLogger.info("Using Kubernetes cloud config: " + kubeCloud);

            ChartRepositoryConfig config = kubeCloud.getChartRepositoryConfiguration(chartsRepo);
            taskLogger.info("Using Chart repository config: " + config);

            Authentication authData = PluginHelper.getAuthenticationData(config.getCredentialsId());
            ChartRepo chartRepo = new ChartRepo(config.getChartsRepoUrl(), authData);

            taskLogger.info("Deploying chart: " + chartName);
            deploymentService.deployChart(cloudName, kubeCloud.getNamespace(), chartRepo, chartName);

        } catch (ServiceException exception) {
            final String message = "Cannot find Kubernetes cloud: " + cloudName;
            taskLogger.error(message);
            throw new IOException(message, exception);

        } catch (NullPointerException exception) {
            final String message = "Cannot initialize resources: " + exception.getMessage();
            taskLogger.error(message);
            throw new IOException(message, exception);
        }
    }

    protected Object readResolve() {
        if (deploymentService == null) {
            ((DescriptorImpl) getDescriptor()).injector.injectMembers(this);
        }
        return this;
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

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private static final String KUBERNETES_DEPLOY_CHART = "Kubernetes - Deploy Chart";

        @Inject
        private Injector injector;

        @Inject
        private transient KubernetesRepository kubeRepository;

        @Inject
        private transient ChartRepository chartRepository;

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return KUBERNETES_DEPLOY_CHART;
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
                return FormValidation.error("Chart to deploy is required");
            }
            return FormValidation.ok();
        }
    }

}
