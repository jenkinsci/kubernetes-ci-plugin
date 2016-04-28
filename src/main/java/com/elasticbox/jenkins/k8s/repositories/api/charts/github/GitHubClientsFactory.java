package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;

/**
 * Created by serna on 4/26/16.
 */
public interface GitHubClientsFactory {

    <T> T getClient(String baseUrl,
                    Authentication authentication,
                    Class<T> serviceTypeInterface,
                    GitHubApiResponseContentType responseType) throws RepositoryException;

    <T> T getClient(String baseUrl,
                    Class<T> serviceTypeInterface,
                    GitHubApiResponseContentType responseType) throws RepositoryException;
}
