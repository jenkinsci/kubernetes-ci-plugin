package com.elasticbox.jenkins.k8s.repositories;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;

import java.util.List;

/**
 * Created by serna on 4/13/16.
 */
public interface ChartRepository {

    List<String> chartNames(String repo, String ref) throws RepositoryException;

    Chart chart(String repo, String name, String ref) throws RepositoryException;

    Chart chart(String repo, String name) throws RepositoryException;

}
