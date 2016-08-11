/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.plugin.builders;

import com.google.inject.Inject;
import com.google.inject.Injector;

import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import com.elasticbox.jenkins.k8s.util.TaskLogger;
import hudson.Extension;
import hudson.model.Run;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.UUID;
import java.util.logging.Logger;

public class DeleteChartBuildStep extends BaseChartBuildStep {

    private static final Logger LOGGER = Logger.getLogger(DeleteChartBuildStep.class.getName() );

    private static final String NAME_PREFIX = "DeleteChartBS-";
    private static final String KUBERNETES_DELETE_CHART = "Kubernetes - Delete Chart";

    @DataBoundConstructor
    public DeleteChartBuildStep(String id, String kubeName, String namespace, String chartsRepo, String chartName) {
        super();
        this.id = StringUtils.isNotEmpty(id)  ? id : NAME_PREFIX + UUID.randomUUID().toString();
        this.kubeName = kubeName;
        this.namespace = namespace;
        this.chartsRepo = chartsRepo;
        this.chartName = chartName;
        injectMembers();
    }

    @Override
    protected void doPerform(Run<?, ?> run, TaskLogger taskLogger, ChartRepo chartRepo)
        throws ServiceException {

        taskLogger.info("Deleting chart: " + getChartName());
        deploymentService.deleteChart(getKubeName(), getNamespace(), chartRepo, getChartName() );
        taskLogger.info("Chart [" + getChartName() + "] deleted");
    }

    @Extension
    public static final class DescriptorImpl extends ChartBuildStepDescriptor {

        public DescriptorImpl() {
            super(DeleteChartBuildStep.class, KUBERNETES_DELETE_CHART);
            LOGGER.warning("No args constructor called. No injection performed!");
        }

        @Inject
        public DescriptorImpl(Injector injector, ChartRepository chartRepository, KubernetesRepository kubeRepository) {
            super(DeleteChartBuildStep.class, injector, chartRepository, kubeRepository, KUBERNETES_DELETE_CHART);
        }
    }

}