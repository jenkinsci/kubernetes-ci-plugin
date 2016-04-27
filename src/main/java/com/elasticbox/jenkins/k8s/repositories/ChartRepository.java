package com.elasticbox.jenkins.k8s.repositories;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;

import java.util.List;

public interface ChartRepository {

    List<String> chartNames(ChartRepo repo) throws RepositoryException;

    List<String> chartNames(ChartRepo repo, String ref) throws RepositoryException;

    Chart chart(ChartRepo repo, String chartName) throws RepositoryException;

    Chart chart(ChartRepo repo, String chartName, String ref) throws RepositoryException;
}
