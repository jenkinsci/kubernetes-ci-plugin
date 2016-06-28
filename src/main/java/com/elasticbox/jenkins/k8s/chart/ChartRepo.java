/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.chart;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubUrl;

public class ChartRepo {

    private GitHubUrl url;
    private Authentication authentication;

    public ChartRepo(String url) {
        this.url = new GitHubUrl(url);
    }

    public ChartRepo(String url, Authentication authentication) {
        this.url = new GitHubUrl(url);
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public boolean needsAuthentication() {
        return authentication != null;
    }

    public GitHubUrl getUrl() {
        return url;
    }

}
