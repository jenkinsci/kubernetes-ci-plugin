package com.elasticbox.jenkins.k8s.services.task;

public interface Task<R> {

    boolean isDone();

    void execute() throws TaskException;

    R getResult();

}
