/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.plugin.clouds;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.SlaveProvisioningService;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import hudson.model.Label;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.net.ssl.SSLHandshakeException;
import java.util.Arrays;
import java.util.List;

public class TestKubernetesCloud extends com.elasticbox.jenkins.k8s.plugin.TestBaseKubernetes {

    @Mock
    protected KubernetesRepository kubernetesRepositoryMock;

    @Mock
    protected SlaveProvisioningService slaveProvisioningServiceMock;

    @Test
    public void testGetKubernetesClouds() {
        final List<KubernetesCloud> kubernetesClouds = KubernetesCloud.getKubernetesClouds();
        Assert.assertEquals("Kubernetes clouds not found.", 1, kubernetesClouds.size() );

        final KubernetesCloud kubernetesCloud = KubernetesCloud.getKubernetesCloud(cloud.getName() );
        Assert.assertNotNull("Kubernetes clouds not found", kubernetesCloud);
        Assert.assertNotNull("Injection failed", kubernetesCloud.kubeFactory);

        Assert.assertEquals(cloud.getDisplayName(), kubernetesCloud.getDisplayName() );
        Assert.assertEquals(cloud.getName(), kubernetesCloud.getName() );
        Assert.assertEquals(FAKE_URL, kubernetesCloud.getEndpointUrl() );
    }

    @Test
    public void testGettersChartRepoCfgsAndCloudParams() {
        final List<ChartRepositoryConfig> chartRepositoryConfigurations = cloud.getChartRepositoryConfigurations();
        Assert.assertEquals("Chart repository configuration not found", 1, chartRepositoryConfigurations.size() );

        ChartRepositoryConfig chartRepoCfg = cloud.getChartRepositoryConfiguration(fakeChartRepoCfg.getDescription());
        Assert.assertNotNull("Chart repository configuration not found", chartRepoCfg);

        // Now testing getKubernetesCloudParams:
        final KubernetesCloudParams kubernetesCloudParams = cloud.getKubernetesCloudParams();
        Assert.assertNotNull("Kubernetes clouds parameteres not found", kubernetesCloudParams);
        Assert.assertEquals(FAKE_URL, kubernetesCloudParams.getEndpointUrl() );
    }

    @Test
    public void testDescriptorFillNamespaceItems() throws RepositoryException, ServiceException {
        final KubernetesCloud.DescriptorImpl cloudDescriptor = (KubernetesCloud.DescriptorImpl) cloud.getDescriptor();
        Assert.assertNotNull("Injection failed", cloudDescriptor.kubeRepository);
        Assert.assertNotNull("Injection failed", cloudDescriptor.getInjector() );

        // First attempts with fake data will fail (expect some errors in logs)
        ListBoxModel namespaceItems = cloudDescriptor.doFillPredefinedNamespaceItems(FAKE_URL, EMPTY, EMPTY);
        Assert.assertEquals("Error filling default namespace list items", 1, namespaceItems.size() );

        FormValidation formValidation = cloudDescriptor.doTestConnection(FAKE_URL, FAKE_NS, EMPTY, EMPTY);
        Assert.assertEquals("Test connection fail was expected", FormValidation.Kind.ERROR, formValidation.kind);

        formValidation = cloudDescriptor.doTestConnection(EMPTY, FAKE_NS, EMPTY, EMPTY);
        Assert.assertEquals("Test connection fail was expected", FormValidation.Kind.ERROR, formValidation.kind);
        Assert.assertTrue("Required fields message was expected", formValidation.getMessage().contains("Required"));

        // Now mocking the repository will return some data:
        initRepositoryMock(cloudDescriptor);

        namespaceItems = cloudDescriptor.doFillPredefinedNamespaceItems(FAKE_URL, EMPTY, EMPTY);
        Assert.assertEquals("Error filling namespace list with 3 items", 3, namespaceItems.size() );

        formValidation = cloudDescriptor.doTestConnection(FAKE_URL, FAKE_NS, EMPTY, EMPTY);
        Assert.assertEquals("Test connection success was expected", FormValidation.Kind.OK, formValidation.kind);

        formValidation = cloudDescriptor.doTestConnection(FAKE_URL, FAKE_NS, EMPTY, EMPTY);
        Assert.assertEquals("Test connection exception was expected", FormValidation.Kind.ERROR, formValidation.kind);
        Assert.assertTrue("Exception message was expected", formValidation.getMessage().contains(FAKE_MOCK_EXCEPTION));

        formValidation = cloudDescriptor.doTestConnection(FAKE_URL, FAKE_NS, EMPTY, EMPTY);
        Assert.assertEquals("Test connection warning was expected", FormValidation.Kind.WARNING, formValidation.kind);
        Assert.assertTrue("'Not trusted' message was expected", formValidation.getMessage().contains("not trusted"));
    }

    @Test
    public void testCanProvision() throws RepositoryException, ServiceException {
        initCanProvisionMocks();

        boolean provision = cloud.canProvision(null);
        Mockito.verify(kubernetesRepositoryMock).testConnection(any(KubernetesCloudParams.class));
        Mockito.verify(slaveProvisioningServiceMock).canProvision(any(KubernetesCloud.class), any(List.class), any(Label.class));
        Assert.assertTrue("Expected provisioning allowed", provision);


        provision = cloud.canProvision(null);
        Mockito.verify(kubernetesRepositoryMock, Mockito.times(2)).testConnection(any(KubernetesCloudParams.class));
        Mockito.verify(slaveProvisioningServiceMock).canProvision(any(KubernetesCloud.class), any(List.class), any(Label.class));
        Assert.assertFalse("Expected provisioning declined", provision);

        provision = cloud.canProvision(null);
        Mockito.verify(kubernetesRepositoryMock, Mockito.times(4)).testConnection(any(KubernetesCloudParams.class));
        Mockito.verify(slaveProvisioningServiceMock, Mockito.times(2)).canProvision(any(KubernetesCloud.class), any(List.class), any(Label.class));
        Assert.assertFalse("Expected provisioning declined", provision);
    }


    private void initRepositoryMock(KubernetesCloud.DescriptorImpl cloudDescriptor) throws RepositoryException, ServiceException {
        MockitoAnnotations.initMocks(this);

        Mockito.when(kubernetesRepositoryMock.namespaceExists(anyString(), anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(kubernetesRepositoryMock.getNamespaces(any(KubernetesCloudParams.class)))
                .thenReturn(Arrays.asList("default", FAKE_NS));
        Mockito.when(kubernetesRepositoryMock.testConnection(any(KubernetesCloudParams.class)))
                .thenReturn(true)
                .thenThrow(new RepositoryException(FAKE_MOCK_EXCEPTION))
                .thenThrow(new RepositoryException("Wrapper", new RepositoryException(
                        "MidCause", new SSLHandshakeException("InitialCause"))))
                .thenReturn(true);

        cloudDescriptor.kubeRepository = kubernetesRepositoryMock;
    }

    private void initCanProvisionMocks() throws RepositoryException, ServiceException {
        initRepositoryMock( (KubernetesCloud.DescriptorImpl) cloud.getDescriptor() );

        Mockito.when(slaveProvisioningServiceMock.canProvision(any(KubernetesCloud.class), any(List.class), any(Label.class)))
                .thenReturn(Boolean.TRUE)
                .thenThrow(new ServiceException(FAKE_MOCK_EXCEPTION));

        cloud.slaveProvisioningService = slaveProvisioningServiceMock;
    }
}