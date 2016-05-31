package com.elasticbox.jenkins.k8s.plugin.slaves;


import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.ExecutorListener;
import hudson.model.OneOffExecutor;
import hudson.model.Queue;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.CloudRetentionStrategy;
import hudson.slaves.EphemeralNode;
import hudson.util.TimeUnit2;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;



public final class OnceRetentionStrategy extends CloudRetentionStrategy implements ExecutorListener {

    private static final Logger LOGGER = Logger.getLogger(OnceRetentionStrategy.class.getName());

    private transient boolean terminating;

    private int idleMinutes;

    public OnceRetentionStrategy(int idleMinutes) {
        super(idleMinutes);
        this.idleMinutes = idleMinutes;
    }

    @Override
    public long check(final AbstractCloudComputer computer) {
        // When the slave is idle we should disable accepting tasks and check to see if it is already trying to
        // terminate. If it's not already trying to terminate then lets terminate manually.
        if (computer.isIdle() && !disabled) {
            final long idleMilliseconds = System.currentTimeMillis() - computer.getIdleStartMilliseconds();
            if (idleMilliseconds > TimeUnit2.MINUTES.toMillis(idleMinutes)) {
                LOGGER.log(Level.FINE, "Disconnecting {0}", computer.getName());
                done(computer);
            }
        }

        // Return one because we want to check every minute if idle.
        return 1;
    }

    @Override public void start(AbstractCloudComputer computer) {
        if (computer.getNode() instanceof EphemeralNode) {
            throw new IllegalStateException("May not use OnceRetentionStrategy on an EphemeralNode: " + computer);
        }
        super.start(computer);
    }

    @Override public void taskAccepted(Executor executor, Queue.Task task) {}

    @Override public void taskCompleted(Executor executor, Queue.Task task, long durationMs) {
        done(executor);
    }

    @Override public void taskCompletedWithProblems(
        Executor executor, Queue.Task task, long durationMs, Throwable problems) {
        done(executor);
    }

    private void done(Executor executor) {
        final AbstractCloudComputer<?> c = (AbstractCloudComputer) executor.getOwner();
        Queue.Executable exec = executor.getCurrentExecutable();
        LOGGER.log(Level.FINE, "terminating {0} since {1} seems to be finished", new Object[] {c.getName(), exec});
        done(c);
    }

    private void done(final AbstractCloudComputer<?> computer) {
        computer.setAcceptingTasks(false); // just in case
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
                // TODO once the baseline is 1.592+ switch to Queue.withLock
                Object queue = jenkins == null ? OnceRetentionStrategy.this : jenkins.getQueue();
                synchronized (queue) {
                    try {
                        AbstractCloudSlave node = computer.getNode();
                        if (node != null) {
                            node.terminate();
                        }
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.WARNING, "Failed to terminate " + computer.getName(), e);
                        synchronized (OnceRetentionStrategy.this) {
                            terminating = false;
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to terminate " + computer.getName(), e);
                        synchronized (OnceRetentionStrategy.this) {
                            terminating = false;
                        }
                    }
                }
            }
        });
    }

}
