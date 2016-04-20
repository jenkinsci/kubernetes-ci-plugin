package com.elasticbox.jenkins.k8s.repositories.error;

/**
 * Created by serna on 4/14/16.
 */
public class RepositoryException extends Exception {
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(String message) {
        super(message);
    }
}
