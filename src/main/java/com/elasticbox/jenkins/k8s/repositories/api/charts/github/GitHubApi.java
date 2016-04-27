package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import com.elasticbox.jenkins.k8s.chart.ChartRepo;

/**
 * Created by serna on 4/27/16.
 */
public interface GitHubApi {

    boolean isApplicableFor(GitHubUrl url);

    String getUrlType();

}
