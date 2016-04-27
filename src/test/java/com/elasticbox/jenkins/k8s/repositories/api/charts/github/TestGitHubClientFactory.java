package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by serna on 4/27/16.
 */
public class TestGitHubClientFactory {

    @Test
    public void testClientFactory() throws RepositoryException {


        GitHubClientsFactoryImpl factory = new GitHubClientsFactoryImpl();


        String publicGitHubRepoUrl = "https://github.com/helm/charts";
        final GitHubApiContentsService client1 = factory.getClient(publicGitHubRepoUrl,
                                                                    GitHubApiContentsService.class,
                                                                    GitHubApiResponseContentType.JSON);

        final GitHubApiContentsService client2 = factory.getClient("https://github.com/oserna/questions",
                                                                    GitHubApiContentsService.class,
                                                                    GitHubApiResponseContentType.JSON);

        assertTrue(client1 == client2);



        String enterpriseGitHubRepoUrl = "https://git.elasticbox.com/serna/jenkins-plugin-kubernetes";
        String enterpriseRawContentUrl = "https://git.elasticbox.com/raw/serna/jenkins-plugin-kubernetes/master/pom.xml?token=AAAAL7xdKEfxQtVtXnmabhzDNdA-d-rrks5XHzQIwA%3D%3D";
        final GitHubApiContentsService client3 = factory.getClient(enterpriseGitHubRepoUrl,
                                                                    GitHubApiContentsService.class,
                                                                    GitHubApiResponseContentType.JSON);

        final GitHubApiRawContentDownloadService client4 = factory.getClient(enterpriseRawContentUrl,
                                                                    GitHubApiRawContentDownloadService.class,
                                                                    GitHubApiResponseContentType.RAW_STRING);
        assertTrue(client3 != client4);


        String publicRawContentUrl = "https://raw.githubusercontent.com/helm/charts/master/rabbitmq/Chart.yaml";
        final GitHubApiRawContentDownloadService client5 = factory.getClient(publicRawContentUrl,
                                                                            GitHubApiRawContentDownloadService.class,
                                                                            GitHubApiResponseContentType.RAW_STRING);
        assertTrue(client5 != client1);
        assertTrue(client5 != client3);
        assertTrue(client5 != client4);


    }
}
