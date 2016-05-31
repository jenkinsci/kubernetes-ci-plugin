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
}
