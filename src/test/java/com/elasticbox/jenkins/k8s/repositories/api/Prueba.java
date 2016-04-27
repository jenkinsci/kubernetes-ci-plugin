package com.elasticbox.jenkins.k8s.repositories.api;

import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubUrl;

import static org.junit.Assert.assertTrue;

/**
 * Created by serna on 4/26/16.
 */
public class Prueba {

    public static void main(String[] args) {


            String urlString = "https://raw.githubusercontent.com/helm/charts/master/rabbitmq/Chart.yaml";

            GitHubUrl url = new GitHubUrl(urlString);

            assertTrue(url.path().equals("helm/charts/master/rabbitmq/Chart.yaml"));
            assertTrue(url.owner().equals("helm"));
            assertTrue(url.repo().equals("charts"));

            final String[] split = url.path().split("/");

            assertTrue(split.length == 5);

    }
}
