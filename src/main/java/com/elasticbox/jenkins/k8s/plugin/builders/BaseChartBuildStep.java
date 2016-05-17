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
        ((ChartBuildStepDescriptor) getDescriptor()).getInjector().injectMembers(this);
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
        final String runName = (run != null) ? run.toString() : "<NO-RUN>";                ;
        taskLogger.info("Executing Chart build step: " + runName);

        try {
            KubernetesCloud kubeCloud = KubernetesCloud.getKubernetesCloud(getCloudName() );
            taskLogger.info("Using Kubernetes clouds config: " + kubeCloud);

            ChartRepositoryConfig config = kubeCloud.getChartRepositoryConfiguration(getChartsRepo() );
            taskLogger.info("Using Chart repository config: " + config);

            Authentication authData = PluginHelper.getAuthenticationData(config.getCredentialsId() );
            ChartRepo chartRepo = new ChartRepo(config.getChartsRepoUrl(), authData);

            doPerform(runName, taskLogger, kubeCloud, chartRepo);

        } catch (ServiceException exception) {
            taskLogger.error(exception.getCausedByMessages() );
            throw new IOException(exception);

        } catch (NullPointerException exception) {
            final String message = "Cannot initialize resources: " + exception.getMessage();
            taskLogger.error(message);
            throw new IOException(message, exception);
        }
    }

    protected abstract void doPerform(String runName, TaskLogger taskLogger,
                                      KubernetesCloud kubeCloud, ChartRepo chartRepo)
            throws ServiceException;

}
