/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.repositories.api.kubeclient;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloudParams;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.client.KubernetesClient;

public interface KubernetesClientFactory {

    KubernetesClient getKubernetesClient(String kubeName) throws RepositoryException;

    KubernetesClient createKubernetesClient(KubernetesCloudParams kubeCloudParams);

    void resetKubernetesClient(String kubeName);
}
