/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AbstractPodDeployment;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;
import com.elasticbox.jenkins.k8s.plugin.slaves.KubernetesSlave;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import com.elasticbox.jenkins.k8s.services.task.ScheduledPoolingTask;
import com.elasticbox.jenkins.k8s.services.task.TaskException;
import hudson.slaves.SlaveComputer;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class WaitForSlaveToBeOnline extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(WaitForSlaveToBeOnline.class.getName());

    private static final long DELAY_IN_SECONDS = 1;
    private static final long INITIAL_DELAY_IN_SECONDS = 0;
    private static final long TIMEOUT_IN_SECONDS = 60;

    private long initialDelay;
    private long delay;
    private long timeout;

    public WaitForSlaveToBeOnline() {
        this(INITIAL_DELAY_IN_SECONDS, DELAY_IN_SECONDS, TIMEOUT_IN_SECONDS);
    }

    /**
     * Only for testing purposes for now.
     */
    public WaitForSlaveToBeOnline(long initialDelay, long delay, long timeout) {
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.timeout = timeout;
    }


    /**
     * Its mission is wait for the Jenkins slave to be online until the specified timeout.
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        try {

            final KubernetesSlave kubernetesSlave = deploymentContext.getKubernetesSlave();

            new WaitForTheSlaveToBeOnlineTask(kubernetesSlave, delay, initialDelay, timeout).execute();

            LOGGER.log(Level.INFO, "Jenkins slave: " + kubernetesSlave.getNodeName() + " is online");

        } catch (TaskException error) {

            LOGGER.log(Level.SEVERE, "Error waiting for the Jenkins slave to be online", error);

            throw new ServiceException("Error waiting for the Jenkins slave to be online", error);

        }

    }


    private static class WaitForTheSlaveToBeOnlineTask extends ScheduledPoolingTask<Boolean> {

        private KubernetesSlave slave;

        public WaitForTheSlaveToBeOnlineTask(KubernetesSlave slave,
                                             long delay,
                                             long initialDelay,
                                             long timeout) {

            super(delay, initialDelay, timeout);

            this.slave = slave;
            this.result = false;
        }

        @Override
        protected void performExecute() throws TaskException {

            final SlaveComputer computer = slave.getComputer();
            if (computer == null) {
                LOGGER.warning("Computer is null for the slave: " + slave);
                return;
            }

            this.result = computer.isOnline();

            LOGGER.log(Level.INFO, "Jenkins slave is online: " + this.result);
        }

        @Override
        public boolean isDone() {
            return result;
        }
    }



}
