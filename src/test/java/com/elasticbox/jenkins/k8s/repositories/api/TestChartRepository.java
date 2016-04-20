package com.elasticbox.jenkins.k8s.repositories.api;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.api.charts.ChartRepositoryApiImpl;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GithubContent;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GithubRawContentService;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GithubService;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GithubUrl;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.Links;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.vmplugin.v5.TestNgUtils;
import org.junit.Test;
import org.mockito.Mockito;
import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by serna on 4/13/16.
 */
public class TestChartRepository {


    @Test
    public void testRepoUrlSlashEnding() throws MalformedURLException {

        String urlString = "https://raw.githubusercontent.com/helm/charts/master/rabbitmq/Chart.yaml";

        GithubUrl url = new GithubUrl(urlString);

        assertTrue(url.path().equals("helm/charts/master/rabbitmq/Chart.yaml"));
        assertTrue(url.owner().equals("helm"));
        assertTrue(url.repo().equals("charts"));

        final String[] split = url.path().split("/");
        assertTrue(split.length == 5);

    }

    @Test
    public void testRepoUrl() throws MalformedURLException {

        String url = "https://api.github.com/repos/helm/charts/git/blobs/af5769570942320a8bd018ae054280028dd5f0c9";

        URL parsedUrl =  new URL(url);

        assertTrue(parsedUrl.getProtocol().equals("https"));
        assertTrue(parsedUrl.getHost().equals("api.github.com"));
        assertTrue(parsedUrl.getQuery() == null);
        assertTrue(parsedUrl.getPath().equals("/repos/helm/charts/git/blobs/af5769570942320a8bd018ae054280028dd5f0c9"));

        final String[] split = parsedUrl.getPath().split("/");
        assertTrue(split.length == 7);

    }


    @Test
    public void testGetChart() throws RepositoryException, IOException {

        final String chart = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/chartYaml.yaml")));

        final String service = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/serviceChartManifest.yaml")));

        final String rc = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/replicationControllerChartManifest.yaml")));

        final GithubRawContentService githubRawContentService = Mockito.mock(GithubRawContentService.class);
        when(githubRawContentService.rawContentFromUrl(any(String.class)))
            .thenReturn(Observable.just(chart))
            .thenReturn(Observable.just(rc))
            .thenReturn(Observable.just(service));

        final List<GithubContent> rootChartContent = Arrays.asList(
            TestUtils.getFakeChartDetails(),
            TestUtils.getFakeReadme(),
            TestUtils.getFakeManifestsFolder()
        );

        final List<GithubContent> manifestsContent = Arrays.asList(
            TestUtils.getFakeReplicationControllerManifest(),
            TestUtils.getFakeServiceManifest()
        );

        final GithubService githubService = Mockito.mock(GithubService.class);
        when(githubService.contents(any(String.class),any(String.class),any(String.class),any(String.class)))
            .thenReturn(Observable.just(rootChartContent));
        when(githubService.contentsFromUrl(any(String.class)))
            .thenReturn(Observable.just(manifestsContent));


        ChartRepositoryApiImpl repository = new ChartRepositoryApiImpl();
        repository.setGithubRawContentService(githubRawContentService);
        repository.setGithubService(githubService);

        final Chart chartModel = repository.chart("https://github.com/helm/charts", "fakeChartName");

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
