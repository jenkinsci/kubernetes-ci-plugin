package com.elasticbox.jenkins.k8s.plugin.slaves;

import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.ExecutorListener;
import hudson.model.Queue;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.CloudRetentionStrategy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleUseRetentionStrategy extends CloudRetentionStrategy implements ExecutorListener {

    private static final Logger LOGGER = Logger.getLogger(KubernetesComputer.class.getName());


    public SingleUseRetentionStrategy(int idleMinutes) {
        super(idleMinutes);
    }

    @Override
    public void taskAccepted(Executor executor, Queue.Task task) {
        LOGGER.fine("Accepted task: " + task.getName());
    }

    @Override
    public void taskCompleted(Executor executor, Queue.Task task, long duration) {

        LOGGER.fine("Completed task: " + task.getName() + " in: " + duration + " ms");

        terminate((AbstractCloudComputer<?>) executor.getOwner());
    }

    @Override
    public void taskCompletedWithProblems(Executor executor, Queue.Task task, long duration, Throwable problems) {

        LOGGER.fine("Task completed with problems: " + task.getName() + " in: " + duration + " ms");

        terminate((AbstractCloudComputer<?>) executor.getOwner());
    }


    private void terminate(final AbstractCloudComputer<?> computer) {

        computer.setAcceptingTasks(false); // just in case

        Computer.threadPoolForRemoting.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    AbstractCloudSlave node = computer.getNode();
                    if (node != null) {
                        node.terminate();
                    }
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Failed to terminate " + computer.getName(), e);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to terminate " + computer.getName(), e);
                }
            }
        });
    }
}
