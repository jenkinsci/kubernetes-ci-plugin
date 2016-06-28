/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.repositories.error;

import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.apache.commons.lang.StringUtils;

public class RepositoryException extends Exception {
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(Throwable cause) {
        super(cause);
    }

    public Throwable getInitialCause() {
        Throwable initialCause = getCause();
        if (initialCause != null) {
            while (initialCause.getCause() != null) {
                initialCause = initialCause.getCause();
            }
            return initialCause;
        }
        return this;
    }

    public String getCausedByMessages() {
        Throwable initialCause = getCause();
        if (initialCause != null) {
            StringBuilder msg = new StringBuilder();
            appendMessage(msg, initialCause);
            while (initialCause.getCause() != null) {
                initialCause = initialCause.getCause();
                appendMessage(msg, initialCause);
            }
            return msg.toString();
        } else {
            return getMessage();
        }
    }

    private void appendMessage(StringBuilder msg, Throwable initialCause) {
        if (initialCause instanceof KubernetesClientException) {
            KubernetesClientException kce = (KubernetesClientException)initialCause;
            final Status status = kce.getStatus();
            if (status != null) {
                msg.append(status.getMessage() ).append(' ');
            }
        } else if (msg.indexOf(initialCause.toString() ) == -1) {
            msg.append(initialCause.toString() ).append(' ');
        }
    }
}
