package com.elasticbox.jenkins.k8s.plugin.slaves;

import hudson.model.Executor;
import hudson.model.Queue;
import hudson.slaves.AbstractCloudComputer;

import java.util.logging.Level;
import java.util.logging.Logger;


public class KubernetesComputer extends AbstractCloudComputer<KubernetesSlave> {

    private static final Logger LOGGER = Logger.getLogger(KubernetesComputer.class.getName());

    public KubernetesComputer(KubernetesSlave slave) {
        super(slave);
    }

    @Override
    public void taskAccepted(Executor executor, Queue.Task task) {
        super.taskAccepted(executor, task);
        LOGGER.fine(" Computer " + this + " taskAccepted");
    }

    @Override
    public void taskCompleted(Executor executor, Queue.Task task, long duration) {

        Queue.Executable executable = executor.getCurrentExecutable();

        LOGGER.log(Level.FINE, " Computer: " + this + " taskCompleted: " + executable);

        // May take the slave offline and remove it, in which case getNode()
        // above would return null and we'd not find our DockerSlave anymore.
        super.taskCompleted(executor, task, duration);
    }

    @Override
    public void taskCompletedWithProblems(Executor executor, Queue.Task task, long duration, Throwable problems) {
        super.taskCompletedWithProblems(executor, task, duration, problems);
        LOGGER.log(Level.FINE, " Computer " + this + " taskCompletedWithProblems");
    }

    @Override
    public String toString() {
        return String.format("KubernetesComputer name: %s slave: %s", getName(), getNode());
    }
}