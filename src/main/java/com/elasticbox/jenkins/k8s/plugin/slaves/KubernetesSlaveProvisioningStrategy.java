package com.elasticbox.jenkins.k8s.plugin.slaves;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import hudson.Extension;
import hudson.model.Label;
import hudson.slaves.CloudProvisioningListener;
import hudson.slaves.NodeProvisioner;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class KubernetesSlaveProvisioningStrategy extends NodeProvisioner.Strategy {
    private static final Logger LOGGER = Logger.getLogger(KubernetesSlaveProvisioningStrategy.class.getName() );

    private NodeProvisioner.StrategyState strategyState;

    @Override
    public NodeProvisioner.StrategyDecision apply(@Nonnull NodeProvisioner.StrategyState state) {
        this.strategyState = state;

        int excessWorkload = state.getSnapshot().getQueueLength()
                - state.getSnapshot().getConnectingExecutors() - state.getPlannedCapacitySnapshot();

        return apply(state.getLabel(), excessWorkload);
    }

    NodeProvisioner.StrategyDecision apply(Label label, int excessWorkload) {

        LOGGER.fine("Applying provisioning for label: " + label + " with excessWorkload: " + excessWorkload);

        if (excessWorkload <= 0) {
            return NodeProvisioner.StrategyDecision.PROVISIONING_COMPLETED;
        }

        for (KubernetesCloud kubeCloud : KubernetesCloud.getKubernetesClouds() ) {
            if (kubeCloud.canProvision(label )) {

                LOGGER.fine("Checking 'canProvision' listeners for cloud: " + kubeCloud);
                for (CloudProvisioningListener listener : CloudProvisioningListener.all() ) {
                    if (listener.canProvision(kubeCloud, label, excessWorkload) != null) {
                        return excessWorkload > 1 ? NodeProvisioner.StrategyDecision.CONSULT_REMAINING_STRATEGIES
                                : NodeProvisioner.StrategyDecision.PROVISIONING_COMPLETED;
                    }
                }

                Collection<NodeProvisioner.PlannedNode> nodes = kubeCloud.provision(label, excessWorkload);

                LOGGER.fine("Calling 'onStarted' listeners with: " + nodes);
                for (CloudProvisioningListener listener : CloudProvisioningListener.all()) {
                    listener.onStarted(kubeCloud, label, nodes);
                }

                for (NodeProvisioner.PlannedNode node : nodes) {
                    excessWorkload -= node.numExecutors;
                    LOGGER.log(Level.INFO, "Started provisioning \"{0}\" from \"{1}\". Remaining excess workload: {2}",
                            new Object[]{node.displayName, kubeCloud.getDisplayName(), excessWorkload});
                }

                if (strategyState != null) {
                    strategyState.recordPendingLaunches(nodes);
                }

                if (excessWorkload <= 0) {
                    return NodeProvisioner.StrategyDecision.PROVISIONING_COMPLETED;
                }
            }
        }

        LOGGER.fine("Provisioning not complete, checking remaining strategies for excess workload: " + excessWorkload);
        return NodeProvisioner.StrategyDecision.CONSULT_REMAINING_STRATEGIES;
    }
}