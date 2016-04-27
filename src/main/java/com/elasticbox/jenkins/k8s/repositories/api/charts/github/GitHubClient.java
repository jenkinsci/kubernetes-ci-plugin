package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

/**
 * Created by serna on 4/26/16.
 */
public class GitHubClient<T> {

    private String apiBaseUrl;
    private T client;

    public GitHubClient(String apiBaseUrl, T client) {
        this.apiBaseUrl = apiBaseUrl;
        this.client = client;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public T getClient() {
        return client;
    }
}
