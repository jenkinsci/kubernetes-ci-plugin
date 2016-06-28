/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

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
