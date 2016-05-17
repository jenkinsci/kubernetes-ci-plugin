package com.elasticbox.jenkins.k8s.plugin.builders;

import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.ChartDeploymentService;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

public class TestDeployChartBuildStep extends com.elasticbox.jenkins.k8s.plugin.TestBaseKubernetes {

    private static final String FAKE_CHART_NAME = "FakeChartName";

    @Mock
    private ChartDeploymentService chartDeploymentServiceMock;

    @Mock
    private ChartRepository chartRepositoryMock;

    private DeployChartBuildStep deployChartBuildStep;

    @Before
    public void setupBuildStep() throws Exception {
        deployChartBuildStep = new DeployChartBuildStep(EMPTY, cloud.name, FAKE_CHARTS_REPO, FAKE_CHART_NAME);
    }

    @Test
    public void testDeployChartBS() throws RepositoryException, ServiceException, InterruptedException, IOException {
        Assert.assertNotNull("Injection failed", deployChartBuildStep.deploymentService);

        MockitoAnnotations.initMocks(this);
        deployChartBuildStep.deploymentService = chartDeploymentServiceMock;

        final LogTaskListener listener = new LogTaskListener(Logger.getLogger(this.getClass().getName()), Level.OFF);

        Mockito.doNothing().when(chartDeploymentServiceMock)
                .deployChart(anyString(), anyString(), any(ChartRepo.class), anyString(), any(Map.class) );

        deployChartBuildStep.perform(null, null,null, listener);
        Mockito.verify(chartDeploymentServiceMock)
                .deployChart(anyString(), anyString(), any(ChartRepo.class), anyString(), any(Map.class) );

        Mockito.doThrow(new ServiceException(FAKE_MOCK_EXCEPTION, new Throwable() ))
                .when(chartDeploymentServiceMock)
                .deployChart(anyString(), anyString(), any(ChartRepo.class), anyString(), any(Map.class) );
        try {
            deployChartBuildStep.perform(null, null,null, listener);
        } catch (IOException e) {
            Mockito.verify(chartDeploymentServiceMock, Mockito.times(2) )
                    .deployChart(anyString(), anyString(), any(ChartRepo.class), anyString(), any(Map.class) );
        }

    }

    @Test
    public void testDescriptor() throws RepositoryException {
        final DeployChartBuildStep.DescriptorImpl descriptor =
                (DeployChartBuildStep.DescriptorImpl) deployChartBuildStep.getDescriptor();
        Assert.assertNotNull("Injection failed", descriptor.chartRepository);
        Assert.assertNotNull("Injection failed", descriptor.getInjector() );

        initChartRepositoryMock();
        descriptor.chartRepository = chartRepositoryMock;

        ListBoxModel items = descriptor.doFillCloudNameItems();
        Assert.assertEquals("Error filling cloud list items", 2, items.size() );

        items = descriptor.doFillChartsRepoItems(cloud.name);
        Assert.assertEquals("Error filling charts repo list items", 2, items.size() );

        items = descriptor.doFillChartNameItems(cloud.name, FAKE_CHARTS_REPO);
        Assert.assertEquals("Error filling chart names list items", 2, items.size() );
    }

    private void initChartRepositoryMock() throws RepositoryException {
        if (chartRepositoryMock == null) {
            MockitoAnnotations.initMocks(this);

            Mockito.when(chartRepositoryMock.chartNames(any(ChartRepo.class) ))
                    .thenReturn(Collections.singletonList(FAKE_CHART_NAME) );
        }
    }
}
