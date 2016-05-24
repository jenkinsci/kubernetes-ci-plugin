package com.elasticbox.jenkins.k8s.plugin.clouds.slaves;

import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.ExecutorListener;
import hudson.model.Queue;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.CloudRetentionStrategy;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleUseRetentionStrategy extends CloudRetentionStrategy implements ExecutorListener {

    private static final Logger LOGGER = Logger.getLogger(JenkinsKubernetesComputer.class.getName());


    public SingleUseRetentionStrategy(int idleMinutes) {
        super(idleMinutes);
    }

    @Override
    public void taskAccepted(Executor executor, Queue.Task task) {
        LOGGER.fine(" Computer " + this + " taskAccepted");

    }

    @Override
    public void taskCompleted(Executor executor, Queue.Task task, long durationMS) {

    }

    @Override
    public void taskCompletedWithProblems(Executor executor, Queue.Task task, long durationMS, Throwable problems) {

    }


    private void done(final AbstractCloudComputer<?> c) {
        c.setAcceptingTasks(false); // just in case
        synchronized (this) {
            if (terminating) {
                return;
            }
            terminating = true;
        }
        Computer.threadPoolForRemoting.submit(new Runnable() {
            @Override
            public void run() {
                final Jenkins jenkins = Jenkins.getInstance();

                Queue.withLock();
                // TODO once the baseline is 1.592+ switch to Queue.withLock
                Object queue = jenkins == null ? OnceRetentionStrategy.this : jenkins.getQueue();
                synchronized (queue) {
                    try {
                        AbstractCloudSlave node = c.getNode();
                        if (node != null) {
                            node.terminate();
                        }
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.WARNING, "Failed to terminate " + c.getName(), e);
                        synchronized (OnceRetentionStrategy.this) {
                            terminating = false;
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to terminate " + c.getName(), e);
                        synchronized (OnceRetentionStrategy.this) {
                            terminating = false;
                        }
                    }
                }
            }
        });
    }
}
