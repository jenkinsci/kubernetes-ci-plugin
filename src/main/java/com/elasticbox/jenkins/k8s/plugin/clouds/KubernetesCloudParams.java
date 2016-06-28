/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.plugin.clouds;

import com.elasticbox.jenkins.k8s.auth.Authentication;

public class KubernetesCloudParams {

    private final String endpointUrl;
    private final String namespace;
    private final Authentication authData;
    private boolean disableCertCheck;
    private final String serverCert;

    public KubernetesCloudParams(String endpointUrl, String namespace,
                                 Authentication authData,  String serverCert) {

        this.endpointUrl = endpointUrl;
        this.namespace = namespace;
        this.authData = authData;
        this.serverCert = serverCert;
        this.disableCertCheck = true;

    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public String getNamespace() {
        return namespace;
    }

    public Authentication getAuthData() {
        return authData;
    }

    public boolean isDisableCertCheck() {
        return disableCertCheck;
    }

    public void setDisableCertCheck(boolean disableCertCheck) {
        this.disableCertCheck = disableCertCheck;
    }

    public String getServerCert() {
        return serverCert;
    }
}
