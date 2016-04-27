package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

/**
 * Created by serna on 4/26/16.
 */
public class GitHubApiRequest {

    public enum GitHubApiResponseType {
        JSON,
        RAW_STRING
    }

    private GitHubUrl gitHubUrl;
    private GitHubApiResponseType responseType;

    public GitHubApiRequest(String repoUrl, GitHubApiResponseType responseType) {
        this.gitHubUrl = new GitHubUrl(repoUrl);
        this.responseType =  responseType;
    }

    public GitHubUrl getGitHubUrl() {
        return gitHubUrl;
    }

    public GitHubApiResponseType getResponseType() {
        return responseType;
    }


}
