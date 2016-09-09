/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;

public interface GitHubClientsFactory {

    <T> T getClient(ChartRepo chartRepo,
                    Class<T> serviceTypeInterface,
                    GitHubApiResponseContentType responseType) throws RepositoryException;
}
