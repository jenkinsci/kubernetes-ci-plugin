/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AbstractPodDeployment;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import com.elasticbox.jenkins.k8s.services.task.ScheduledPoolingTask;
import com.elasticbox.jenkins.k8s.services.task.TaskException;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class WaitForPodToBeRunning extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(WaitForPodToBeRunning.class.getName());

    @Inject
    private PodRepository podRepository;

    private static final long DELAY_IN_SECONDS = 1;
    private static final long INITIAL_DELAY_IN_SECONDS = 1;
    private static final long TIMEOUT_IN_SECONDS = 90;

    private long initialDelay;
    private long delay;
    private long timeout;

    public WaitForPodToBeRunning() {
        this(INITIAL_DELAY_IN_SECONDS, DELAY_IN_SECONDS, TIMEOUT_IN_SECONDS);
    }

    /**
     * Only for testing purposes for now.
     */
    public WaitForPodToBeRunning(long initialDelay, long delay, long timeout) {
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.timeout = timeout;
    }

    /**
     * Its mission is wait for the Pod to be running until the specified timeout.
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        final String podName = deploymentContext.getPodToDeploy().getMetadata().getName();
        final KubernetesCloud kubeCloud = deploymentContext.getCloudToDeployInto();

        try {
            new WaitForThePodToBeRunningTask(
                podRepository,
                kubeCloud.getName(),
                kubeCloud.getPredefinedNamespace(),
                podName,
                delay,
                initialDelay,
                timeout).execute();

            LOGGER.log(Level.INFO, "Pod is up and running");

        } catch (TaskException error) {
            LOGGER.severe("Error waiting for the Pod to be running: " + podName);
            throw new ServiceException("Error waiting for the Pod to be running", error);
        }
    }

    public static enum PodState {

        PENDING("Pending"),
        RUNNING("Running"),
        SUCCEEDED("Succeeded"),
        FAILED("Failed"),
        UNKNOWN("Unknown");

        private String status;

        PodState(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public static PodState findByDescription(String status) {
            for (PodState state: PodState.values()) {
                if (state.getStatus().equals(status)) {
                    return state;
                }
            }
            return UNKNOWN;
        }
    }

    private static class WaitForThePodToBeRunningTask extends ScheduledPoolingTask<PodState> {

        private String kubeName;
        private String namespace;
        private String podName;
        private PodRepository podRepository;

        public WaitForThePodToBeRunningTask(PodRepository podRepository,
                                            String kubeName,
                                            String namespace,
                                            String podName,
                                            long delay,
                                            long initialDelay,
                                            long timeout) {

            super(delay, initialDelay, timeout);

            this.podRepository = podRepository;
            this.kubeName = kubeName;
            this.podName = podName;
            this.namespace = namespace;
            this.result = PodState.UNKNOWN;
        }

        @Override
        protected void performExecute() throws TaskException {

            try {
                final Pod pod = podRepository.getPod(kubeName, namespace, podName);

                final String phase = pod.getStatus().getPhase();

                final PodState podStatus = PodState.findByDescription(phase);

                if (podStatus == PodState.FAILED) {
                    LOGGER.log(Level.INFO, "Pod is at the Failed stage");
                    throw new TaskException("Pod deployment failed");
                }

                this.result = podStatus;

                LOGGER.info("Pod: " + pod.getMetadata().getName() + " is at the " + result.getStatus() + "stage");

            } catch (RepositoryException exception) {
                String message = "Error getting pod in cloud: " + kubeName + " in namespace: " + namespace
                        + " and pod name : " + podName;

                LOGGER.log(Level.SEVERE, message, exception);
                throw new TaskException(message, exception);
            }

        }

        @Override
        public boolean isDone() {
            final PodState result = this.getResult();
            return result == PodState.RUNNING;
        }
    }



}
