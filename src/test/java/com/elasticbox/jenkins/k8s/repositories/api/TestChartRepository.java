package com.elasticbox.jenkins.k8s.repositories.api;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.repositories.api.charts.ChartRepositoryApiImpl;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubApiContentsService;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubApiRawContentDownloadService;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubClientsFactoryImpl;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubContent;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;
import rx.Observable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by serna on 4/13/16.
 */
public class TestChartRepository {

    @Test
    public void testGetChart() throws RepositoryException, IOException {

        final String chart = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/chartYaml.yaml")));

        final String service = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/serviceChartManifest.yaml")));

        final String rc = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/replicationControllerChartManifest.yaml")));

        // fake content of: https://api.github.com/repositories/44991456/contents/rabbitmq
        final List<GitHubContent> rootChartContent = Arrays.asList(
            TestUtils.getFakeChartDetails(),
            TestUtils.getFakeReadme(),
            TestUtils.getFakeManifestsFolder()
        );

        // fake content of: https://api.github.com/repos/helm/charts/contents/rabbitmq/manifests
        final List<GitHubContent> manifestsContent = Arrays.asList(
            TestUtils.getFakeReplicationControllerManifest(),
            TestUtils.getFakeServiceManifest()
        );

        final GitHubApiContentsService githubService = Mockito.mock(GitHubApiContentsService.class);
        when(githubService.content(any(String.class), any(String.class), any(String.class), any(String.class)))
            .thenReturn(Observable.just(rootChartContent))
            .thenReturn(Observable.just(manifestsContent));

        final GitHubApiRawContentDownloadService gitHubApiRawContentDownloadService = Mockito.mock(GitHubApiRawContentDownloadService.class);
        when(gitHubApiRawContentDownloadService.rawContent(any(String.class)))
            .thenReturn(Observable.just(chart))
            .thenReturn(Observable.just(rc))
            .thenReturn(Observable.just(service));

        ChartRepositoryApiImpl repository = new ChartRepositoryApiImpl();
        repository.setClientsFactory(new GitHubClientsFactoryImpl());

        ChartRepo fakeRepo = new ChartRepo("https://github.com/helm/charts");

        final Chart chartModel = repository.chart(fakeRepo, "rabbitmq");

        assertTrue(chartModel.getName().equals("rabbitmq"));
        assertTrue(chartModel.getHome().equals("https://www.rabbitmq.com/"));
        assertTrue(chartModel.getVersion().equals("0.2.0"));
        assertTrue(chartModel.getDescription().equals("Chart running RabbitMQ."));
        assertTrue(chartModel.getMaintainers().length==1);
        assertTrue(chartModel.getDetails().equals("This package provides RabbitMQ (a message broker) for development purposes."));

        final ReplicationController[] replicationControllers = chartModel.getReplicationControllers();
        final Service[] services = chartModel.getServices();

        assertTrue(replicationControllers.length == 1);
        assertTrue(services.length == 1);

        ReplicationController rcModel = replicationControllers[0];
        assertTrue(rcModel.getApiVersion().toString().equals("v1"));
        assertTrue(rcModel.getKind().equals("ReplicationController"));

        assertTrue(rcModel.getMetadata().getName().equals("rabbitmq"));
        assertTrue(rcModel.getMetadata().getLabels().equals(new HashMap<String, String>(){{
            put("provider","rabbitmq");
            put("heritage","helm");
        }}));

        assertTrue(rcModel.getSpec().getReplicas()==1);

        assertTrue(rcModel.getSpec().getTemplate().getMetadata().getLabels().equals(new HashMap<String, String>(){{
            put("provider","rabbitmq");
        }}));

        final Container container = new Container();
        container.setName("rabbitmq");
        container.setImage("rabbitmq:3.6.0");
        container.setPorts(new ArrayList<ContainerPort>(){{add(new ContainerPort(5672,null,null,null,null));}});
        final List<Container> containerList = Arrays.asList(container);

        assertTrue(rcModel.getSpec().getTemplate().getSpec().getContainers().equals(containerList));

    }

}
