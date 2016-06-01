package com.elasticbox.jenkins.k8s.services;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;

import java.util.Map;

public interface ChartDeploymentService {

    Chart deployChart(String kubeName, String namespace, ChartRepo chartRepo, String chartName,
                      Map<String, String> label) throws ServiceException;

    void deleteChart(String kubeName, String namespace, ChartRepo chartRepo, String chartName) throws ServiceException;

    void deleteChart(String kubeName, String namespace, Chart chart) throws ServiceException;
}
