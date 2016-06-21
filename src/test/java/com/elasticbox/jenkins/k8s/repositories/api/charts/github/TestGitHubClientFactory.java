/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.auth.TokenAuthentication;
import com.elasticbox.jenkins.k8s.auth.UserAndPasswordAuthentication;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestGitHubClientFactory {

    @Test
    public void testGitHubClientFactoryCountClients() throws RepositoryException {

        String publicGitHubRepoUrl = "https://github.com/helm/charts";
        String enterpriseGitHubRepoUrl = "https://git.elasticbox.com/serna/jenkins-plugin-kubernetes";
        String publicRawContentUrl = "https://raw.githubusercontent.com/helm/charts/master/rabbitmq/Chart.yaml";
        String enterpriseRawContentUrl = "https://git.elasticbox.com/raw/serna/jenkins-plugin-kubernetes/master/pom.xml?token=AAAAL7xdKEfxQtVtXnmabhzDNdA-d-rrks5XHzQIwA%3D%3D";

        Authentication token = new TokenAuthentication("fakeToken");
        Authentication password = new UserAndPasswordAuthentication("fakeUser", "fakePassword");

        GitHubClientsFactoryImpl factory = new GitHubClientsFactoryImpl();

        factory.getClient(
            publicGitHubRepoUrl,
            token,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        factory.getClient(
            enterpriseGitHubRepoUrl,
            token,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        factory.getClient(
            publicRawContentUrl,
            token,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        factory.getClient(
            enterpriseRawContentUrl,
            token,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        assertTrue(factory.getCache().size() == 4);


    }


    @Test
    public void testGitHubClientFactory() throws RepositoryException {

        String publicGitHubRepoUrl = "https://github.com/helm/charts";
        String enterpriseGitHubRepoUrl = "https://git.elasticbox.com/serna/jenkins-plugin-kubernetes";
        String publicRawContentUrl = "https://raw.githubusercontent.com/helm/charts/master/rabbitmq/Chart.yaml";
        String enterpriseRawContentUrl = "https://git.elasticbox.com/raw/serna/jenkins-plugin-kubernetes/master/pom.xml?token=AAAAL7xdKEfxQtVtXnmabhzDNdA-d-rrks5XHzQIwA%3D%3D";

        Authentication token = new TokenAuthentication("fakeToken");
        Authentication password = new UserAndPasswordAuthentication("fakeUser", "fakePassword");

        GitHubClientsFactoryImpl factory = new GitHubClientsFactoryImpl();


        final GitHubApiContentsService client = factory.getClient(
            publicGitHubRepoUrl,
            token,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        final GitHubApiContentsService client2 = factory.getClient(
            publicGitHubRepoUrl,
            token,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        assertTrue(client == client2);

        final GitHubApiContentsService client3 = factory.getClient(
            publicGitHubRepoUrl,
            password,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        assertTrue(client3 != client2);

        final GitHubApiContentsService client4 = factory.getClient(
            publicGitHubRepoUrl,
            password,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        assertTrue(client3 == client4);

        assertTrue(factory.getCache().size() == 2);

        final GitHubApiContentsService client5 = factory.getClient(
            enterpriseRawContentUrl,
            password,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        assertTrue(factory.getCache().size() == 3);

        final GitHubApiContentsService client6 = factory.getClient(
            enterpriseRawContentUrl,
            password,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        assertTrue(client6 == client5);
        assertTrue(factory.getCache().size() == 3);

        final GitHubApiContentsService client7 = factory.getClient(
            enterpriseGitHubRepoUrl,
            password,
            GitHubApiContentsService.class,
            GitHubApiResponseContentType.JSON);

        assertTrue(client7 != client6);
        assertTrue(factory.getCache().size() == 4);

        System.out.println();

    }

}
