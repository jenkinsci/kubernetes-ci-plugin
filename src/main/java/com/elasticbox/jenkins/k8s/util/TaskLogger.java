/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.util;

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