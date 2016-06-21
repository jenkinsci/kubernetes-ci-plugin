/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.services;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfigurationParams;
import com.elasticbox.jenkins.k8s.plugin.slaves.KubernetesSlave;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.model.Label;

import java.util.List;

public interface SlaveProvisioningService {


    KubernetesSlave slaveProvision(KubernetesCloud kubernetesCloud,
                                   List<PodSlaveConfigurationParams> podConfigurations,
                                   Label label) throws ServiceException;

    boolean canProvision(KubernetesCloud kubernetesCloud,
                            List<PodSlaveConfigurationParams> podConfigurations,
                            Label label) throws ServiceException;

}
