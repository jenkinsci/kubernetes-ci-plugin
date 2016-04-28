package com.elasticbox.jenkins.k8s.chart;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.auth.TokenAuthentication;
import com.elasticbox.jenkins.k8s.auth.UserAndPasswordAuthentication;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubUrl;


public class ChartRepo {

    private GitHubUrl url;
    private Authentication authentication;

    public ChartRepo(String url) {
        this.url = new GitHubUrl(url);
    }

    public ChartRepo(String url, UserAndPasswordAuthentication authentication) {
        this.url = new GitHubUrl(url);
        this.authentication = authentication;
    }

    public ChartRepo(String url, TokenAuthentication authentication) {
        this.url = new GitHubUrl(url);
        this.authentication = authentication;
    }


    public Authentication getAuthentication() {
        return authentication;
    }

    public boolean needsAthentication() {
        return authentication != null;
    }

    public GitHubUrl getUrl() {
        return url;
    }

}
