package com.elasticbox.jenkins.k8s;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.chart.ChartDetails;
import com.elasticbox.jenkins.k8s.repositories.api.charts.factory.ManifestFactory;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubContent;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.util.TestUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Created by serna on 5/19/16.
 */
public class Test {

    public static void main(String[] args) throws RepositoryException, IOException {
        final String yaml = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/podChartManifest.yaml")));

        ChartDetails fakeDetails = new ChartDetails();
        fakeDetails.setDescription("fakeDescription");
        fakeDetails.setDetails("fakeDetails");
        fakeDetails.setHome("fakeHome");
        fakeDetails.setMaintainers(Arrays.asList("fakeMantainer"));
        fakeDetails.setName("fakeName");
        fakeDetails.setSource("fakeSource");
        fakeDetails.setVersion("fakeVersion");

        Chart.ChartBuilder builder = new Chart.ChartBuilder();
        builder.chartDetails(fakeDetails);

        ManifestFactory.addManifest(yaml, builder);

        final Chart build = builder.build();

        assertTrue("At least it should contains one rc", 1 == build.getPods().size() );
    }
}
