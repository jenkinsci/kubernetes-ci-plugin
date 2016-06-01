package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps;

import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.AbstractPodDeployment;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;
import com.elasticbox.jenkins.k8s.plugin.slaves.KubernetesSlave;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import com.elasticbox.jenkins.k8s.services.task.ScheduledPoolingTask;
import com.elasticbox.jenkins.k8s.services.task.TaskException;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class WaitForSlaveToBeOnline extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(WaitForSlaveToBeOnline.class.getName());

    private static final long DELAY_SECONDS = 1;
    private static final long INITIAL_DELAY = 0;
    private static final long TIMEOUT = 60;


    /**
     * Its mission is wait for the Jenkins slave to be online until the specified timeout.
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        try {

            final KubernetesSlave kubernetesSlave = deploymentContext.getKubernetesSlave();

            new WaitForTheSlaveToBeOnlineTask(kubernetesSlave, DELAY_SECONDS, INITIAL_DELAY, TIMEOUT).execute();

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


            if (slave.getComputer() == null) {
                LOGGER.warning("Computer is null for the slave: " + slave);
                return;
            }

            this.result = slave.getComputer().isOnline();

            LOGGER.log(Level.INFO, "Jenkins slave is online: " + this.result);
        }

        @Override
        public boolean isDone() {
            return result;
        }
    }



}
