package com.elasticbox.jenkins.k8s.plugin.builders;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.chart.ChartDetails;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.plugin.TestBaseKubernetes;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.ChartDeploymentService;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import hudson.util.LogTaskListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestChartBuildSteps extends TestBaseKubernetes {

    private static final String FAKE_CHART_NAME = "FakeChartName";

    @Mock
    private ChartDeploymentService chartDeploymentServiceMock;

    @Mock
    private ChartRepository chartRepositoryMock;

    @Before
    public void setupMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDeployChartBS()
            throws RepositoryException, ServiceException, InterruptedException, IOException, ExecutionException {

        DeployChartBuildStep deployChartBuildStep = getFakeDeployChartBuildStep(false);

        Mockito.when(chartDeploymentServiceMock
                .deployChart(anyString(), anyString(), any(ChartRepo.class), anyString(), any(Map.class) ))
                .thenReturn(getFakeChart() );

        // Test only deploy chart:
        Result result = executeBuild(deployChartBuildStep);

        Assert.assertTrue("Build result was not successful", result.isBetterOrEqualTo(result.SUCCESS) );
        Mockito.verify(chartDeploymentServiceMock)
                .deployChart(anyString(), anyString(), any(ChartRepo.class), anyString(), any(Map.class) );
        Mockito.verify(chartDeploymentServiceMock, Mockito.times(0) )
                .deleteChart(anyString(), anyString(), any(Chart.class) );

        // Test deploy and delete chart:
        deployChartBuildStep = getFakeDeployChartBuildStep(true);
        result = executeBuild(deployChartBuildStep);

        Assert.assertTrue("Build result was not successful", result.isBetterOrEqualTo(result.SUCCESS) );
        Mockito.verify(chartDeploymentServiceMock, Mockito.times(2) )
                .deployChart(anyString(), anyString(), any(ChartRepo.class), anyString(), any(Map.class) );
        Mockito.verify(chartDeploymentServiceMock, Mockito.times(1) )
                .deleteChart(anyString(), anyString(), any(Chart.class) );

        // Test exception:
        Mockito.doThrow(new ServiceException(FAKE_MOCK_EXCEPTION, new Throwable() ))
                .when(chartDeploymentServiceMock)
                .deployChart(anyString(), anyString(), any(ChartRepo.class), anyString(), any(Map.class) );

        try {
            result = executeBuild(deployChartBuildStep);
        } catch (IOException e) {
            Mockito.verify(chartDeploymentServiceMock, Mockito.times(3) )
                    .deployChart(anyString(), anyString(), any(ChartRepo.class), anyString(), any(Map.class) );
            Mockito.verify(chartDeploymentServiceMock, Mockito.times(1) )
                    .deleteChart(anyString(), anyString(), any(Chart.class) );
        }
    }

    private Chart getFakeChart() throws RepositoryException {
        final ChartDetails details = new ChartDetails();
        details.setMaintainers(Collections.EMPTY_LIST);
        return new Chart.ChartBuilder().chartDetails(details).build();
    }

    private Result executeBuild(Builder buildStep) throws IOException, InterruptedException, ExecutionException {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(buildStep);

        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        if ( !build.getResult().isCompleteBuild() ) {
            Thread.sleep(5000);
        }
        return build.getResult();
    }

    @Test
    public void testDeleteChartBS() throws RepositoryException, ServiceException, InterruptedException, IOException {
        final DeleteChartBuildStep deleteChartBuildStep =
                new DeleteChartBuildStep(EMPTY, cloud.name, FAKE_CHARTS_REPO, FAKE_CHART_NAME);

        Assert.assertNotNull("Injection failed", deleteChartBuildStep.deploymentService);

        deleteChartBuildStep.deploymentService = chartDeploymentServiceMock;

        final LogTaskListener listener = new LogTaskListener(Logger.getLogger(this.getClass().getName()), Level.OFF);

        Mockito.doNothing().when(chartDeploymentServiceMock)
                .deleteChart(anyString(), anyString(), any(ChartRepo.class), anyString() );

        deleteChartBuildStep.perform(null, null,null, listener);

        Mockito.verify(chartDeploymentServiceMock)
                .deleteChart(anyString(), anyString(), any(ChartRepo.class), anyString() );

        Mockito.doThrow(new ServiceException(FAKE_MOCK_EXCEPTION, new Throwable() ))
                .when(chartDeploymentServiceMock)
                .deleteChart(anyString(), anyString(), any(ChartRepo.class), anyString() );

        try {
            deleteChartBuildStep.perform(null, null,null, listener);
        } catch (IOException e) {
            Mockito.verify(chartDeploymentServiceMock, Mockito.times(2) )
                    .deleteChart(anyString(), anyString(), any(ChartRepo.class), anyString() );
        }
    }

    @Test
    public void testDescriptor() throws RepositoryException {
        final DeployChartBuildStep.DescriptorImpl descriptor =
                (DeployChartBuildStep.DescriptorImpl) getFakeDeployChartBuildStep(false).getDescriptor();

        Assert.assertNotNull("Injection failed", descriptor.chartRepository);
        Assert.assertNotNull("Injection failed", descriptor.getInjector() );

        Mockito.when(chartRepositoryMock.chartNames(any(ChartRepo.class) ))
                .thenReturn(Collections.singletonList(FAKE_CHART_NAME) );

        descriptor.chartRepository = chartRepositoryMock;

        ListBoxModel items = descriptor.doFillCloudNameItems();
        Assert.assertEquals("Error filling cloud list items", 2, items.size() );
        Assert.assertTrue("Unique item not selected", items.get(1).selected);

        items = descriptor.doFillChartsRepoItems(null);
        Assert.assertEquals("Error filling default charts repo list items", 1, items.size() );
        Assert.assertFalse("Invalid item selected", items.get(0).selected);

        items = descriptor.doFillChartsRepoItems(cloud.name);
        Assert.assertEquals("Error filling charts repo list items", 2, items.size() );
        Assert.assertTrue("Unique item not selected", items.get(1).selected);

        items = descriptor.doFillChartNameItems(null, null);
        Assert.assertEquals("Error filling default chart names list items", 1, items.size() );
        Assert.assertFalse("Invalid item selected", items.get(0).selected);

        items = descriptor.doFillChartNameItems(cloud.name, FAKE_CHARTS_REPO);
        Assert.assertEquals("Error filling chart names list items", 2, items.size() );
        Assert.assertFalse("Invalid item selected", items.get(0).selected);
        Assert.assertFalse("Invalid item selected", items.get(1).selected);
    }

    private DeployChartBuildStep getFakeDeployChartBuildStep(boolean alsoDeleteChart) {

        DeployChartBuildStep deployChartBuildStep =
                new DeployChartBuildStep(EMPTY, cloud.name, FAKE_CHARTS_REPO, FAKE_CHART_NAME, alsoDeleteChart);

        Assert.assertNotNull("Injection failed", deployChartBuildStep.deploymentService);
        deployChartBuildStep.deploymentService = chartDeploymentServiceMock;

        return deployChartBuildStep;
    }
}
