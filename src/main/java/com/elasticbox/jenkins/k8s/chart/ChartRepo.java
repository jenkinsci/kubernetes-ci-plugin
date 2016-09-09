/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.chart;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.auth.UserAndPasswordAuthentication;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubUrl;

import java.net.Proxy;

public class ChartRepo {

    private GitHubUrl url;
    private Authentication authentication;
    private UserAndPasswordAuthentication proxyAuthentication;

    private Proxy proxy;

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

    public void setProxy(Proxy proxy) {
        this.proxy = (proxy != Proxy.NO_PROXY) ? proxy : null;
    }

    public boolean needsProxy() {
        return proxy != null;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxyAuthentication(UserAndPasswordAuthentication proxyAuthentication) {
        this.proxyAuthentication = proxyAuthentication;
    }

    public boolean needsProxyAuthentication() {
        return proxyAuthentication != null;
    }

    public UserAndPasswordAuthentication getProxyAuthentication() {
        return proxyAuthentication;
    }
}
