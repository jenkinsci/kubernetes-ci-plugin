package com.elasticbox.jenkins.k8s.repositories.api.charts.factory;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.chart.ChartDetails;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Created by serna on 4/20/16.
 */
public class TestChartFactory {

    @Test
    public void testPodCreationFromYaml() throws IOException, RepositoryException {
        final String yaml = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/podChartManifest.yaml")));

        final ChartDetails fakeDetails = getFakeChartDetails();

        Chart.ChartBuilder builder = new Chart.ChartBuilder();
        builder.chartDetails(fakeDetails);

        ManifestFactory.addManifest(yaml, builder);

        final Chart build = builder.build();

        assertTrue("At least it should contains one rc", 1 == build.getPods().size() );

    }

    @Test
    public void testReplicationControllerCreationFromYaml() throws IOException, RepositoryException {
        final String yaml = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/replicationControllerChartManifest.yaml")));

        final ChartDetails fakeDetails = getFakeChartDetails();

        Chart.ChartBuilder builder = new Chart.ChartBuilder();
        builder.chartDetails(fakeDetails);

        ManifestFactory.addManifest(yaml, builder);

        final Chart build = builder.build();

        assertTrue("At least it should contains one rc", 1 == build.getReplicationControllers().size() );

    }

    @Test
    public void testServiceCreationFromYaml() throws IOException, RepositoryException {

        final String yaml = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/serviceChartManifest.yaml")));

        ChartDetails fakeDetails = getFakeChartDetails();

        Chart.ChartBuilder builder = new Chart.ChartBuilder();
        builder.chartDetails(fakeDetails);

        ManifestFactory.addManifest(yaml, builder);

        final Chart build = builder.build();


        assertTrue("At least it should contains one service", 1 == build.getServices().size() );

        assertTrue("At least it should contains one service", build.getServices().get(0).getKind().equals("Service"));
        final Service.ApiVersion apiVersion = build.getServices().get(0).getApiVersion();
        assertTrue("At least it should contains one service", apiVersion.toString().equals("v1"));

        final ServiceSpec spec = build.getServices().get(0).getSpec();
        assertTrue("At least it should contains one service", spec.getPorts().get(0).getPort() == 5672 );

        assertTrue("At least it should contains one service", build.getServices().get(0).getSpec().getSelector()
            .containsKey("provider"));
        assertTrue("At least it should contains one service", build.getServices().get(0).getSpec().getSelector().get
            ("provider").equals("rabbitmq"));

    }

    private ChartDetails getFakeChartDetails() {
        ChartDetails fakeDetails = new ChartDetails();
        fakeDetails.setDescription("fakeDescription");
        fakeDetails.setDetails("fakeDetails");
        fakeDetails.setHome("fakeHome");
        fakeDetails.setMaintainers(Arrays.asList("fakeMantainer"));
        fakeDetails.setName("fakeName");
        fakeDetails.setSource("fakeSource");
        fakeDetails.setVersion("fakeVersion");
        return fakeDetails;
    }
}
