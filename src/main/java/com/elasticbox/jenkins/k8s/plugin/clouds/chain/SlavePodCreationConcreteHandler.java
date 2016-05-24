package com.elasticbox.jenkins.k8s.plugin.clouds.chain;

import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;

public interface SlavePodCreationConcreteHandler {

    void handle(JenkinsPodSlaveDeploymentContext deploymentContext) throws RepositoryException;

}
