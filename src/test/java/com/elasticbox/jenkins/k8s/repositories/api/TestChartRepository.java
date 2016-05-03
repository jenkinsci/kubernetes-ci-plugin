package com.elasticbox.jenkins.k8s.repositories.api;

import static org.junit.Assert.assertTrue;

import com.elasticbox.jenkins.k8s.auth.TokenAuthentication;
import com.elasticbox.jenkins.k8s.auth.UserAndPasswordAuthentication;
import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.repositories.api.charts.ChartRepositoryApiImpl;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubClientsFactoryImpl;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TestChartRepository {

    @Test
    public void testGetChartFromPublicRepo() throws RepositoryException, IOException {

        ChartRepositoryApiImpl repository = new ChartRepositoryApiImpl();
        repository.setClientsFactory(TestUtils.getGitHubClientsFactoryMock() );

        ChartRepo fakeRepo = new ChartRepo("https://github.com/fakeOwner/fakeChartsRepo");

        final Chart chartModel = repository.chart(fakeRepo, "fakeChartName");

        assertTrue(chartModel.getName().equals("rabbitmq"));
        assertTrue(chartModel.getHome().equals("https://www.rabbitmq.com/"));
        assertTrue(chartModel.getVersion().equals("0.2.0"));
        assertTrue(chartModel.getDescription().equals("Chart running RabbitMQ."));
        assertTrue(chartModel.getMaintainers().size()==1);
        assertTrue(chartModel.getDetails().equals("This package provides RabbitMQ (a message broker) for development purposes."));


        final List<ReplicationController> replicationControllers = chartModel.getReplicationControllers();
        final List<Service> services = chartModel.getServices();

        assertTrue(replicationControllers.size() == 1);
        assertTrue(services.size() == 1);

        ReplicationController rcModel = replicationControllers.get(0);
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



    @Test
    public void testGetChartFromRepoRequiringUserAndPassword() throws IOException, RepositoryException,
        InterruptedException {

        final String rootChartsRepoContent = IOUtils.toString(new FileInputStream(new File("src/test/resources/rootChartsRepoContent.json")));

        MockWebServer server = new MockWebServer();
        server.start(9999);

        final HttpUrl url = server.url("http://127.0.0.1:9999/fakeOwner/fakeChartsRepo");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(rootChartsRepoContent));

        ChartRepo fakeRepo = new ChartRepo(
            "http://127.0.0.1:9999/fakeOwner/fakeChartsRepo",
            new UserAndPasswordAuthentication("user", "password"));

        ChartRepositoryApiImpl repository = new ChartRepositoryApiImpl();
        repository.setClientsFactory(new GitHubClientsFactoryImpl());

        final List<String> charts = repository.chartNames(fakeRepo);

        final RecordedRequest recordedRequest = server.takeRequest();
        assertTrue("Basic dXNlcjpwYXNzd29yZA==".equals(recordedRequest.getHeader("Authorization")));

        server.shutdown();

    }

    @Test
    public void testGetChartFromRepoRequiringToken() throws IOException, RepositoryException,
        InterruptedException {

        final String rootChartsRepoContent = IOUtils.toString(new FileInputStream(new File
            ("src/test/resources/rootChartsRepoContent.json")));

        MockWebServer server = new MockWebServer();
        server.start(9999);

        final HttpUrl url = server.url("http://127.0.0.1:9999/fakeOwner/fakeChartsRepo");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(rootChartsRepoContent));

        ChartRepo fakeRepo = new ChartRepo(
            "http://127.0.0.1:9999/fakeOwner/fakeChartsRepo",
            new TokenAuthentication("fakeToken"));

        ChartRepositoryApiImpl repository = new ChartRepositoryApiImpl();
        repository.setClientsFactory(new GitHubClientsFactoryImpl());

        final List<String> charts = repository.chartNames(fakeRepo);

        final RecordedRequest recordedRequest = server.takeRequest();
        assertTrue("token fakeToken".equals(recordedRequest.getHeader("Authorization")));

        server.shutdown();

    }


}
