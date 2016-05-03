package com.elasticbox.jenkins.k8s.services.error;

import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;

public class ServiceException extends Exception {
    public ServiceException(RepositoryException exception) {
        super(exception);
    }
}
