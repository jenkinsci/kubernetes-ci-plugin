/*
 * ElasticBox Confidential
 * Copyright (c) 2014 All Right Reserved, ElasticBox Inc.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of ElasticBox. The intellectual and technical concepts contained herein are
 * proprietary and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from ElasticBox.
 */

package com.elasticbox.jenkins.k8s.plugin.util;

import hudson.model.TaskListener;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskLogger {
    private static final String PREFIX = "[Kubernetes cloud] - ";
    private final TaskListener taskListener;
    private final Logger logger;

    public TaskLogger(TaskListener listener, Logger logger) {
        taskListener = listener;
        this.logger = logger;
    }

    public TaskListener getTaskListener() {
        return taskListener;
    }

    public void info(String msg) {
        logger.info(msg);
        taskListener.getLogger().println(PREFIX + msg);
    }

    public void info(String format, Object... args) {
        logger.log(Level.INFO, format, args);
        taskListener.getLogger().println(PREFIX + MessageFormat.format(format, args) );
    }

    public void error(String msg) {
        logger.severe(msg);
        taskListener.error(PREFIX + msg + "\n");
    }

    public void error(String format, Object... args) {
        logger.log(Level.SEVERE, format, args);
        taskListener.error(PREFIX + MessageFormat.format(format, args) + "\n");
    }
}