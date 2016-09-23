/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.plugin.slaves;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.JNLPLauncher;
import hudson.slaves.NodeProperty;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KubernetesSlave extends AbstractCloudSlave {

    public static final String DEFAULT_REMOTE_FS = "/home/jenkins";
    public static final String DESCRIPTION = "Jenkins Kubernetes Slave";

    private static final int IDLE_MINUTES = 1;
    private static final int EXECUTORS = 1;

    private static final Logger LOGGER = Logger.getLogger(KubernetesSlave.class.getName());

    private static final long serialVersionUID = -8642936855413034232L;

    private final transient KubernetesCloud kubernetesCloud;
    private final transient PodRepository podRepository;

    public KubernetesSlave(String podName, PodRepository podRepository, KubernetesCloud kubernetesCloud, Label label)
        throws Descriptor.FormException,IOException {

        super(podName,
            DESCRIPTION,
            DEFAULT_REMOTE_FS,
            EXECUTORS,
            Mode.NORMAL,
            label == null ? null : label.toString(),
            new JNLPLauncher(),
            new SingleUseRetentionStrategy(IDLE_MINUTES),
            Collections.<NodeProperty<Node>>emptyList());

        this.kubernetesCloud = kubernetesCloud;
        this.podRepository = podRepository;
    }

    @Override
    protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
        if (podRepository == null) {
            LOGGER.warning("Unable to terminate Kubernetes instance. Unknown reference: " + name);
            return;
        }
        LOGGER.info("Terminating Kubernetes instance for slave: " + name);

        if (this.toComputer() != null ) {
            try {
                podRepository.delete(kubernetesCloud.getName(), kubernetesCloud.getPredefinedNamespace(), name);

                if (LOGGER.isLoggable(Level.FINE) ) {
                    LOGGER.fine("Terminated Kubernetes instance for slave: " + name);
                }
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Failure to terminate instance for slave: " + name, exception);
            }
        } else {
            LOGGER.warning("There is no computer for slave: " + name);
        }
    }

    @Override
    public KubernetesComputer createComputer() {
        return new KubernetesComputer(this);
    }

    @Override
    public String toString() {
        return String.format("KubernetesSlave name: %s", name);
    }

}
