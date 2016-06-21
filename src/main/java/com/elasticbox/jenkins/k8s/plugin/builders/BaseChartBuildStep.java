/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.plugin.builders;

import com.google.inject.Inject;

import com.elasticbox.jenkins.k8s.util.PluginHelper;
import com.elasticbox.jenkins.k8s.util.TaskLogger;
import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.plugin.clouds.ChartRepositoryConfig;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.services.ChartDeploymentService;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class BaseChartBuildStep extends Builder implements SimpleBuildStep {
    private static final Logger LOGGER = Logger.getLogger(BaseChartBuildStep.class.getName() );

    protected String id;
    protected String kubeName;
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

    public String getKubeName() {
        return kubeName;
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
            KubernetesCloud kubeCloud = KubernetesCloud.getKubernetesCloud(getKubeName() );
            taskLogger.info("Using Kubernetes clouds config: " + kubeCloud);

            ChartRepositoryConfig config = kubeCloud.getChartRepositoryConfiguration(getChartsRepo() );
            taskLogger.info("Using Chart repository config: " + config);

            Authentication authData = PluginHelper.getAuthenticationData(config.getCredentialsId());
            ChartRepo chartRepo = new ChartRepo(config.getChartsRepoUrl(), authData);

            doPerform(run, taskLogger, kubeCloud, chartRepo);

        } catch (ServiceException exception) {
            taskLogger.error(exception.getCausedByMessages() );
            throw new IOException(exception);

        } catch (NullPointerException exception) {
            final String message = "Cannot initialize resources: " + exception.getMessage();
            taskLogger.error(message);
            throw new IOException(message, exception);
        }
    }

    protected abstract void doPerform(Run<?, ?> run, TaskLogger taskLogger,
                                      KubernetesCloud kubeCloud, ChartRepo chartRepo) throws ServiceException;

}
