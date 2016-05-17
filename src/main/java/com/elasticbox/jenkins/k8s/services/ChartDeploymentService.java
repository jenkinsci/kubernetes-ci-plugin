package com.elasticbox.jenkins.k8s.services;

import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;

import java.util.Map;

public interface ChartDeploymentService {

    void deployChart(String kubeName, String namespace, ChartRepo chartRepo, String chartName,
                     Map<String, String> label) throws ServiceException;

    void deleteChart(String cloudName, String namespace, ChartRepo chartRepo, String chartName) throws ServiceException;
}
