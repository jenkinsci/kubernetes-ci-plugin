package com.elasticbox.jenkins.k8s.plugin.builders;

import com.google.inject.Inject;
import com.google.inject.Injector;

import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.util.TaskLogger;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.Extension;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class DeployChartBuildStep extends BaseChartBuildStep {
    private static final Logger LOGGER = Logger.getLogger(DeployChartBuildStep.class.getName() );

    private static final String NAME_PREFIX = "DeployChartBS-";
    private static final String KUBERNETES_DEPLOY_CHART = "Kubernetes - Deploy Chart";

    @DataBoundConstructor
    public DeployChartBuildStep(String id, String cloudName, String chartsRepo, String chartName) {
        super();
        this.id = StringUtils.isNotEmpty(id)  ? id : NAME_PREFIX + UUID.randomUUID().toString();
        this.cloudName = cloudName;
        this.chartsRepo = chartsRepo;
        this.chartName = chartName;
        injectMembers();
    }

    @Override
    protected void doPerform(String runName, TaskLogger taskLogger, KubernetesCloud kubeCloud, ChartRepo chartRepo)
            throws ServiceException {

        taskLogger.info("Deploying chart: " + getChartName());

        Map<String, String> label = Collections.singletonMap("jenkinsJob",
                                        StringUtils.deleteWhitespace(runName).replace('#', '_') );

        deploymentService.deployChart(getCloudName(), kubeCloud.getNamespace(), chartRepo, getChartName(), label);
        taskLogger.info("Chart [" + getChartName() + "] deployed");
    }

    @Extension
    public static final class DescriptorImpl extends ChartBuildStepDescriptor {

        public DescriptorImpl() {
            this(null, null);
            LOGGER.warning("No args constructor called. No injection performed!");
        }

        @Inject
        public DescriptorImpl(Injector injector, ChartRepository chartRepository) {
            super(DeployChartBuildStep.class, injector, chartRepository, KUBERNETES_DEPLOY_CHART);
        }
    }

}
