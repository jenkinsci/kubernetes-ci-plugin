package com.elasticbox.jenkins.k8s.plugin.builders;

import com.google.inject.Inject;
import com.google.inject.Injector;

import com.elasticbox.jenkins.k8s.util.TaskLogger;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.Extension;
import hudson.model.Run;
import hudson.tasks.Builder;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.UUID;
import java.util.logging.Logger;

public class DeleteChartBuildStep extends BaseChartBuildStep {

    private static final Logger LOGGER = Logger.getLogger(DeleteChartBuildStep.class.getName() );

    private static final String NAME_PREFIX = "DeleteChartBS-";
    private static final String KUBERNETES_DELETE_CHART = "Kubernetes - Delete Chart";

    @DataBoundConstructor
    public DeleteChartBuildStep(String id, String kubeName, String chartsRepo, String chartName) {
        super();
        this.id = StringUtils.isNotEmpty(id)  ? id : NAME_PREFIX + UUID.randomUUID().toString();
        this.kubeName = kubeName;
        this.chartsRepo = chartsRepo;
        this.chartName = chartName;
        injectMembers();
    }

    @Override
    protected void doPerform(Run<?, ?> run, TaskLogger taskLogger, KubernetesCloud kubeCloud, ChartRepo chartRepo)
        throws ServiceException {

        taskLogger.info("Deleting chart: " + getChartName());
        deploymentService.deleteChart(getKubeName(), kubeCloud.getNamespace(), chartRepo, getChartName() );
        taskLogger.info("Chart [" + getChartName() + "] deleted");
    }

    @Extension
    public static final class DescriptorImpl extends ChartBuildStepDescriptor {

        public DescriptorImpl() {
            super(DeleteChartBuildStep.class, KUBERNETES_DELETE_CHART);
            LOGGER.warning("No args constructor called. No injection performed!");
        }

        @Inject
        public DescriptorImpl(Injector injector, ChartRepository chartRepository) {
            super(DeleteChartBuildStep.class, injector, chartRepository, KUBERNETES_DELETE_CHART);
        }
    }

}