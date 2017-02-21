package com.elasticbox.jenkins.k8s.plugin.slaves;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import hudson.model.Label;
import hudson.model.Node;
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
    private KubernetesCloud kubeCloudMock1, kubeCloudMock2;

    @Before
    public void setupMocks() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(KubernetesCloud.class);
        PowerMockito.when(KubernetesCloud.getKubernetesClouds()).thenReturn(Arrays.asList(kubeCloudMock1, kubeCloudMock2) );

        NodeProvisioner.PlannedNode node = new NodeProvisioner.PlannedNode("fakeNode", getFakeFuture(), 1);

        when(kubeCloudMock1.getDisplayName() ).thenReturn("fakeKubeCloud1");
        when(kubeCloudMock1.canProvision(null)).thenReturn(false);
        when(kubeCloudMock1.canProvision((Label) notNull())).thenReturn(true);
        when(kubeCloudMock1.provision(any(Label.class), anyInt() )).thenReturn(Collections.<NodeProvisioner.PlannedNode>singleton(node) );

        when(kubeCloudMock2.getDisplayName() ).thenReturn("fakeKubeCloud2");
        when(kubeCloudMock2.canProvision(any(Label.class))).thenReturn(true);
        when(kubeCloudMock2.provision(any(Label.class), anyInt() )).thenReturn(Collections.<NodeProvisioner.PlannedNode>singleton(node) );

    }

    @Test
    public void testProvisioningStrategy_withoutExcessWorkload() {
        KubernetesSlaveProvisioningStrategy strategy = new KubernetesSlaveProvisioningStrategy();
        NodeProvisioner.StrategyDecision decision = strategy.apply(new LabelAtom("fakeLabel"), 0);

        assertEquals("Strategy decision not expected", NodeProvisioner.StrategyDecision.PROVISIONING_COMPLETED, decision);
        Mockito.verify(kubeCloudMock1, Mockito.never() ).provision(any(Label.class), anyInt() );
    }

    @Test
    public void testProvisioningStrategy_withEnoughCapacity() {
        KubernetesSlaveProvisioningStrategy strategy = new KubernetesSlaveProvisioningStrategy();
        NodeProvisioner.StrategyDecision decision = strategy.apply(new LabelAtom("fakeLabel"), 2);

        assertEquals("Strategy decision not expected", NodeProvisioner.StrategyDecision.PROVISIONING_COMPLETED, decision);
        Mockito.verify(kubeCloudMock1).provision(any(Label.class), anyInt());
    }

    @Test
    public void testProvisioningStrategy_withoutEnoughCapacity() {
        KubernetesSlaveProvisioningStrategy strategy = new KubernetesSlaveProvisioningStrategy();
        NodeProvisioner.StrategyDecision decision = strategy.apply(new LabelAtom("fakeLabel"), 3);

        assertEquals("Strategy decision not expected", NodeProvisioner.StrategyDecision.CONSULT_REMAINING_STRATEGIES, decision);
        Mockito.verify(kubeCloudMock1).provision(any(Label.class), anyInt() );
        Mockito.verify(kubeCloudMock2).provision(any(Label.class), anyInt() );
    }

    @Test
    public void testProvisioningStrategy_withNoProvisionableLabelOnFirstCloud() {
        KubernetesSlaveProvisioningStrategy strategy = new KubernetesSlaveProvisioningStrategy();
        NodeProvisioner.StrategyDecision decision = strategy.apply(null, 2);

        assertEquals("Strategy decision not expected", NodeProvisioner.StrategyDecision.CONSULT_REMAINING_STRATEGIES, decision);
        Mockito.verify(kubeCloudMock1, Mockito.never() ).provision(any(Label.class), anyInt() );
        Mockito.verify(kubeCloudMock2).provision(any(Label.class), anyInt() );
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
