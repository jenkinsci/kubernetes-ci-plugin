/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TestLogHandler extends Handler {

    List<String> logList = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
        logList.add(record.getMessage() );
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {

    }

    public void clear() {
        logList.clear();
    }

    public boolean isLogMessageFound(String message) {
        for (String logMessage: logList) {
            if (logMessage.contains(message) ) {
                return true;
            }
        }
        return false;
    }
}