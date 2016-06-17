/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 * Created by serna on 4/27/16.
 */
public class TestGitHubUrl {

    @Test
    public void testRepoUrlSlashEnding() throws MalformedURLException {

        String urlString = "https://raw.githubusercontent.com/helm/charts/master/rabbitmq/Chart.yaml";

        GitHubUrl url = new GitHubUrl(urlString);

        assertTrue(url.path().equals("helm/charts/master/rabbitmq/Chart.yaml"));
        assertTrue(url.ownerInCaseOfRepoUrl().equals("helm"));
        assertTrue(url.repoInCaseOfRepoUrl().equals("charts"));

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
    public void testFindApiBaseUrlFromUrl() {

        String publicGitHubRepoUrl = "https://github.com/helm/charts";
        String apiBaseUrl = GitHubApiType.findOrComposeApiBaseUrl(publicGitHubRepoUrl);
        assertTrue(apiBaseUrl.equals("https://api.github.com/"));

        String enterpriseGitHubRepoUrl = "https://git.elasticbox.com/serna/jenkins-plugin-kubernetes";
        apiBaseUrl = GitHubApiType.findOrComposeApiBaseUrl(enterpriseGitHubRepoUrl);
        assertTrue(apiBaseUrl.equals("https://git.elasticbox.com/api/v3/"));

        String publicRawContentUrl = "https://raw.githubusercontent.com/helm/charts/master/rabbitmq/Chart.yaml";
        apiBaseUrl = GitHubApiType.findOrComposeApiBaseUrl(publicRawContentUrl);
        assertTrue(apiBaseUrl.equals("https://raw.githubusercontent.com/"));

        String enterpriseRawContentUrl = "https://git.elasticbox.com/raw/serna/jenkins-plugin-kubernetes/master/pom.xml?token=AAAAL7xdKEfxQtVtXnmabhzDNdA-d-rrks5XHzQIwA%3D%3D";
        apiBaseUrl = GitHubApiType.findOrComposeApiBaseUrl(enterpriseRawContentUrl);
        assertTrue(apiBaseUrl.equals("https://git.elasticbox.com/raw/"));

    }

}
