package com.elasticbox.jenkins.k8s.plugin.slaves;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import hudson.model.Label;
import hudson.model.LoadStatistics;
import hudson.model.Node;
import hudson.model.OverallLoadStatistics;
import hudson.model.labels.LabelAtom;
import hudson.remoting.Future;
import hudson.slaves.NodeProvisioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KubernetesCloud.class)
public class TestSlaveProvisioningStrategy {

    @Mock
    private KubernetesCloud kubeCloudMock;

    @Before
    public void setupMocks() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(KubernetesCloud.class);
        PowerMockito.when(KubernetesCloud.getKubernetesClouds()).thenReturn(Collections.singletonList(kubeCloudMock) );

        when(kubeCloudMock.getDisplayName() ).thenReturn("fakeKubeCloud");
        when(kubeCloudMock.canProvision(any(Label.class))).thenReturn(true);
        NodeProvisioner.PlannedNode node = new NodeProvisioner.PlannedNode("fakeNode", getFakeFuture(), 1);
        when(kubeCloudMock.provision(any(Label.class), anyInt() )).thenReturn(Collections.<NodeProvisioner.PlannedNode>singleton(node) );
    }

    @Test
    public void testProvisioningStrategy_withoutExcessWorkload() {
        KubernetesSlaveProvisioningStrategy strategy = new KubernetesSlaveProvisioningStrategy();
        NodeProvisioner.StrategyDecision decision = strategy.apply(new LabelAtom("fakeLabel"), 0);

        assertEquals(decision, NodeProvisioner.StrategyDecision.PROVISIONING_COMPLETED);
        Mockito.verify(kubeCloudMock, Mockito.never() ).provision(any(Label.class), anyInt() );
    }

    @Test
    public void testProvisioningStrategy_withEnoughCapacity() {
        KubernetesSlaveProvisioningStrategy strategy = new KubernetesSlaveProvisioningStrategy();
        NodeProvisioner.StrategyDecision decision = strategy.apply(new LabelAtom("fakeLabel"), 1);

        assertEquals(decision, NodeProvisioner.StrategyDecision.PROVISIONING_COMPLETED);
        Mockito.verify(kubeCloudMock).provision(any(Label.class), anyInt());
    }

    @Test
    public void testProvisioningStrategy_withoutEnoughCapacity() {
        KubernetesSlaveProvisioningStrategy strategy = new KubernetesSlaveProvisioningStrategy();
        NodeProvisioner.StrategyDecision decision = strategy.apply(new LabelAtom("fakeLabel"), 2);

        assertEquals(decision, NodeProvisioner.StrategyDecision.CONSULT_REMAINING_STRATEGIES);
        Mockito.verify(kubeCloudMock).provision(any(Label.class), anyInt() );
    }

    private Future<Node> getFakeFuture() {
        return new Future<Node>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) { return false; }

            @Override
            public boolean isCancelled() { return false; }

            @Override
            public boolean isDone() { return false; }

            @Override
            public Node get() throws InterruptedException, ExecutionException { return null; }

            @Override
            public Node get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };
    }

}
