/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

/**
 * Created by serna on 4/26/16.
 */
public class GitHubClient<T> {

    private String apiBaseUrl;
    private T client;

    public GitHubClient(String apiBaseUrl, T client) {
        this.apiBaseUrl = apiBaseUrl;
        this.client = client;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public  T getClient() {
        return client;
    }
}
