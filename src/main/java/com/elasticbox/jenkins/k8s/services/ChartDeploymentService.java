/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

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
