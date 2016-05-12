package com.elasticbox.jenkins.k8s.plugin.clouds;

import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

public class TestKubernetesCloud extends com.elasticbox.jenkins.k8s.plugin.TestBaseKubernetes {

    @Mock
    protected KubernetesRepository kubernetesRepositoryMock;

    @Test
    public void testGetKubernetesClouds() {
        final List<KubernetesCloud> kubernetesClouds = KubernetesCloud.getKubernetesClouds();
        Assert.assertEquals("Kubernetes clouds not found", 1, kubernetesClouds.size() );

        final KubernetesCloud kubernetesCloud = KubernetesCloud.getKubernetesCloud(cloud.name);
        Assert.assertNotNull("Kubernetes clouds not found", kubernetesCloud);
        Assert.assertNotNull("Injection failed", kubernetesCloud.kubeFactory);

        Assert.assertEquals(cloud.getDescription(), kubernetesCloud.getDescription() );
        Assert.assertEquals(cloud.name, kubernetesCloud.name);
        Assert.assertEquals(FAKE_URL, kubernetesCloud.getEndpointUrl() );
    }

    @Test
    public void testGetttersChartRepoCfgsAndCloudParams() {
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
    public void testDescriptorFillNamespaceItems() throws RepositoryException {
        final KubernetesCloud.DescriptorImpl cloudDescriptor = (KubernetesCloud.DescriptorImpl) cloud.getDescriptor();
        Assert.assertNotNull("Injection failed", cloudDescriptor.kubeRepository);
        Assert.assertNotNull("Injection failed", cloudDescriptor.getInjector() );

        // First attempts with fake data will fail (expect some errors in logs)
        ListBoxModel namespaceItems = cloudDescriptor.doFillNamespaceItems(FAKE_URL, EMPTY, true, EMPTY);
        Assert.assertEquals("Error filling default namespace list items", 1, namespaceItems.size() );

        FormValidation formValidation = cloudDescriptor.doTestConnection(FAKE_URL, FAKE_NS, EMPTY, true, EMPTY);
        Assert.assertEquals("Test connection fail was expected", FormValidation.Kind.ERROR, formValidation.kind);

        formValidation = cloudDescriptor.doTestConnection(EMPTY, FAKE_NS, EMPTY, true, EMPTY);
        Assert.assertEquals("Test connection fail was expected", FormValidation.Kind.ERROR, formValidation.kind);
        Assert.assertTrue("Required fields message was expected", formValidation.getMessage().contains("Required"));

        // Now mocking the repository will return some data:
        initMock();
        cloudDescriptor.kubeRepository = kubernetesRepositoryMock;

        namespaceItems = cloudDescriptor.doFillNamespaceItems(FAKE_URL, EMPTY, true, EMPTY);
        Assert.assertEquals("Error filling namespace list with 3 items", 3, namespaceItems.size() );

        formValidation = cloudDescriptor.doTestConnection(FAKE_URL, FAKE_NS, EMPTY, true, EMPTY);
        Assert.assertEquals("Test connection success was expected", FormValidation.Kind.OK, formValidation.kind);

        formValidation = cloudDescriptor.doTestConnection(FAKE_URL, FAKE_NS, EMPTY, true, EMPTY);
        Assert.assertEquals("Test connection exception was expected", FormValidation.Kind.ERROR, formValidation.kind);
        Assert.assertTrue("Exception message was expected", formValidation.getMessage().contains(FAKE_MOCK_EXCEPTION));
    }

    private void initMock() throws RepositoryException {
        if (kubernetesRepositoryMock == null) {
            MockitoAnnotations.initMocks(this);

            Mockito.when(kubernetesRepositoryMock.namespaceExists(anyString(), anyString())).thenReturn(Boolean.TRUE);
            Mockito.when(kubernetesRepositoryMock.getNamespaces(any(KubernetesCloudParams.class)))
                    .thenReturn(Arrays.asList("default", FAKE_NS));
            Mockito.when(kubernetesRepositoryMock.testConnection(any(KubernetesCloudParams.class)))
                    .thenReturn(true)
                    .thenThrow(new RepositoryException(FAKE_MOCK_EXCEPTION));
        }
    }
}
