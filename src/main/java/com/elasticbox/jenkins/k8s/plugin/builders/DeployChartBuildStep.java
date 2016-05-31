package com.elasticbox.jenkins.k8s.plugin.builders;

import com.google.inject.Inject;
import com.google.inject.Injector;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.util.TaskLogger;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.FreeStyleBuild;
import hudson.model.Run;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class DeployChartBuildStep extends BaseChartBuildStep {
    private static final Logger LOGGER = Logger.getLogger(DeployChartBuildStep.class.getName() );

    private static final String NAME_PREFIX = "DeployChartBS-";
    private static final String KUBERNETES_DEPLOY_CHART = "Kubernetes - Deploy Chart";
    private static final String JENKINS_JOB = "elastickube.com/jenkins-build";

    private final boolean deleteChartWhenFinished;

    @DataBoundConstructor
    public DeployChartBuildStep(String id, String cloudName, String chartsRepo, String chartName,
                                boolean deleteChartWhenFinished) {
        super();
        this.id = StringUtils.isNotEmpty(id)  ? id : NAME_PREFIX + UUID.randomUUID().toString();
        this.cloudName = cloudName;
        this.chartsRepo = chartsRepo;
        this.chartName = chartName;
        this.deleteChartWhenFinished = deleteChartWhenFinished;
        injectMembers();
    }

    public boolean getDeleteChartWhenFinished() {
        return deleteChartWhenFinished;
    }

    @Override
    protected void doPerform(Run<?, ?> run, TaskLogger taskLogger, KubernetesCloud kubeCloud, ChartRepo chartRepo)
            throws ServiceException {

        taskLogger.info("Deploying chart: " + getChartName() );

        final String namespace = kubeCloud.getNamespace();
        final String runName = (run != null) ? run.toString() : "<NO-RUN>";

        Map<String, String> label = Collections.singletonMap(JENKINS_JOB,
                                        StringUtils.deleteWhitespace(runName).replace('#', '_') );

        final Chart chart = deploymentService.deployChart(getCloudName(), namespace, chartRepo, chartName, label);
        taskLogger.info("Chart [" + chartName + "] deployed");

        if (deleteChartWhenFinished && run instanceof FreeStyleBuild) {
            taskLogger.info("Chart [" + chartName + "] will be deleted at the end of the run");
            final DeployChartCleanup chartCleanup = new DeployChartCleanup(this, namespace, chart, taskLogger);
            ((FreeStyleBuild) run).getEnvironments().add(0, chartCleanup);
        }
    }

    public static class DeployChartCleanup extends Environment {

        DeployChartBuildStep deployer;
        String namespace;
        Chart chart;
        TaskLogger taskLogger;

        public DeployChartCleanup() {
            LOGGER.warning("Called no-argument constructor. No cleanup will be performed.");
        }

        public DeployChartCleanup(DeployChartBuildStep deployer, String namespace, Chart chart, TaskLogger taskLogger) {
            this.deployer = deployer;
            this.namespace = namespace;
            this.chart = chart;
            this.taskLogger = taskLogger;
        }

        @Override
        public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {

            if (chart == null) {
                return true;
            }

            try {
                deployer.deploymentService.deleteChart(deployer.cloudName, namespace, chart);
                taskLogger.info("Chart [" + chart.getName() + "] successfully removed");

            } catch (ServiceException excep) {
                taskLogger.error("Error while cleaning up chart [" + chart.getName() + "]: " + excep
                        + ". Initial cause: " + excep.getCausedByMessages() );
            }
            return true;
        }

    }

    @Extension
    public static final class DescriptorImpl extends ChartBuildStepDescriptor {

        public DescriptorImpl() {
            super(DeployChartBuildStep.class, KUBERNETES_DEPLOY_CHART);
            LOGGER.warning("No args constructor called. No injection performed!");
        }

        @Inject
        public DescriptorImpl(Injector injector, ChartRepository chartRepository) {
            super(DeployChartBuildStep.class, injector, chartRepository, KUBERNETES_DEPLOY_CHART);
        }
    }

}
