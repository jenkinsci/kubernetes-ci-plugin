package com.elasticbox.jenkins.k8s.plugin.clouds.chain;

/**
 * Created by serna on 5/12/16.
 */
public abstract class AbstractPodDeploymentConcreteHandler implements SlavePodCreationConcreteHandler {

    private SlavePodCreationConcreteHandler nextElementInChain;

    public void setNextElementInChain(SlavePodCreationConcreteHandler nextElementInChain) {
        this.nextElementInChain = nextElementInChain;
    }
}
