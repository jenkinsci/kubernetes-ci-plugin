package com.elasticbox.jenkins.k8s.services;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfig;
import com.elasticbox.jenkins.k8s.plugin.clouds.PodSlaveConfigurationParams;
import com.elasticbox.jenkins.k8s.plugin.slaves.KubernetesSlave;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.api.kubeclient.KubernetesClientFactory;
import com.elasticbox.jenkins.k8s.repositories.api.kubeclient.KubernetesClientFactoryImpl;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.PodDeploymentContext;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.SlaveProvisioningStep;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps.AddSlaveToJenkinsCloud;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps.CheckProvisioningAllowed;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps.CreatePodFromPodConfiguration;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps.PodDeployer;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps.SelectSuitablePodConfiguration;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps.WaitForPodToBeRunning;
import com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain.steps.WaitForSlaveToBeOnline;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import hudson.model.Label;
import hudson.model.labels.LabelOperatorPrecedence;
import hudson.model.labels.LabelVisitor;
import hudson.slaves.SlaveComputer;
import hudson.util.VariableResolver;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KubernetesSlave.class ,SlaveComputer.class})
@PowerMockIgnore({"javax.crypto.*" })
public class TestSlaveProvisioningFailing {

    private Injector injector;

    @Rule
    JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() throws Exception {

        injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {

                bind(SlaveProvisioningService.class)
                    .to(SlaveProvisioningServiceImpl.class)
                    .in(Singleton.class);

                final KubernetesRepository mockKubernetesRepository = Mockito.mock(KubernetesRepository.class);
                bind(KubernetesRepository.class)
                    .toInstance(mockKubernetesRepository);

                PodRepository mockPodsRepository = Mockito.mock(PodRepository.class);
                bind(PodRepository.class)
                    .toInstance(mockPodsRepository);

                bind(KubernetesClientFactory.class)
                    .to(KubernetesClientFactoryImpl.class)
                    .in(Singleton.class);


                Multibinder<SlaveProvisioningStep> podCreationChainHandlers =
                    Multibinder.newSetBinder(binder(), SlaveProvisioningStep.class);

                podCreationChainHandlers.addBinding().to(CheckProvisioningAllowed.class);
                podCreationChainHandlers.addBinding().to(AddSlaveToJenkinsCloud.class);
                podCreationChainHandlers.addBinding().to(SelectSuitablePodConfiguration.class);
                podCreationChainHandlers.addBinding().to(CreatePodFromPodConfiguration.class);
                podCreationChainHandlers.addBinding().to(PodDeployer.class);
                podCreationChainHandlers.addBinding().toInstance(new WaitForPodToBeRunning(0, 1, 5));
                podCreationChainHandlers.addBinding().toInstance(new WaitForSlaveToBeOnline(0, 1, 5));

            }
        });
    }
    @Test(expected=ServiceException.class)
    public void testSlaveProvisioningWithoutLabelAndItNeverGetsOnline() throws Exception {

        final KubernetesCloud mockKubernetesCloud = Mockito.mock(KubernetesCloud.class);
        when(mockKubernetesCloud.getInstanceCap()).thenReturn(10);
        when(mockKubernetesCloud.getName()).thenReturn("FakeCloudName");
        when(mockKubernetesCloud.getNamespace()).thenReturn("FakeNamespace");

        final String podYamlDefault = getPodYamlDefault();
        final PodSlaveConfig fakePodSlaveConfig = getFakePodSlaveConfig(podYamlDefault);

        List<PodSlaveConfig> podSlaveConfigurations = new ArrayList<>();
        podSlaveConfigurations.add(fakePodSlaveConfig);

        final Pod pod = new DefaultKubernetesClient()
            .pods()
            .inNamespace(mockKubernetesCloud.getNamespace())
            .load(IOUtils.toInputStream(fakePodSlaveConfig.getPodYaml()))
            .get();

        pod.setStatus(new PodStatus(null,null,null,null,"Running",null,null,null));

        final PodRepository podRepository = injector.getInstance(PodRepository.class);
        when(podRepository.getPod(anyString(), anyString(), anyString())).thenReturn(pod);
        when(podRepository.pod(anyString(), anyString(), anyString())).thenReturn(pod);
        doNothing().when(podRepository).create(anyString(), anyString(), any(Pod.class));

        final List<PodSlaveConfigurationParams> podSlaveConfigurationParams = new ArrayList<>();
        for (PodSlaveConfig config: podSlaveConfigurations) {
            podSlaveConfigurationParams.add(config.getPodSlaveConfigurationParams());
        }

        final SlaveProvisioningService slaveProvisioningService = injector.getInstance(SlaveProvisioningService.class);
        slaveProvisioningService.slaveProvision(mockKubernetesCloud, podSlaveConfigurationParams, null);



    }

    @Test(expected=ServiceException.class)
    public void testSlaveProvisioningWithoutLabelWithPodFailing() throws Exception {

        final KubernetesCloud mockKubernetesCloud = Mockito.mock(KubernetesCloud.class);
        when(mockKubernetesCloud.getInstanceCap()).thenReturn(10);
        when(mockKubernetesCloud.getName()).thenReturn("FakeCloudName");
        when(mockKubernetesCloud.getNamespace()).thenReturn("FakeNamespace");

        final String podYamlDefault = getPodYamlDefault();
        final PodSlaveConfig fakePodSlaveConfig = getFakePodSlaveConfig(podYamlDefault);

        List<PodSlaveConfig> podSlaveConfigurations = new ArrayList<>();
        podSlaveConfigurations.add(fakePodSlaveConfig);

        final Pod pod = new DefaultKubernetesClient()
            .pods()
            .inNamespace(mockKubernetesCloud.getNamespace())
            .load(IOUtils.toInputStream(fakePodSlaveConfig.getPodYaml()))
            .get();

        pod.setStatus(new PodStatus(null,null,null,null,"Failed",null,null,null));

        final PodRepository podRepository = injector.getInstance(PodRepository.class);
        when(podRepository.getPod(anyString(), anyString(), anyString())).thenReturn(pod);
        when(podRepository.pod(anyString(), anyString(), anyString())).thenReturn(pod);
        doNothing().when(podRepository).create(anyString(), anyString(), any(Pod.class));

        final List<PodSlaveConfigurationParams> podSlaveConfigurationParams = new ArrayList<>();
        for (PodSlaveConfig config: podSlaveConfigurations) {
            podSlaveConfigurationParams.add(config.getPodSlaveConfigurationParams());
        }

        final SlaveProvisioningService slaveProvisioningService = injector.getInstance(SlaveProvisioningService.class);
        slaveProvisioningService.slaveProvision(mockKubernetesCloud, podSlaveConfigurationParams, null);



    }


    public String getPodYamlDefault() throws IOException {
        final String podYaml = IOUtils.toString(new FileInputStream(new File
            ("src/main/resources/com/elasticbox/jenkins/k8s/plugin/clouds/PodSlaveConfig/default-jenkins-slave-pod" +
                ".yaml")));
        return podYaml;
    }

    public PodSlaveConfig getFakePodSlaveConfig(String podYaml) {

        PodSlaveConfig podParams = new PodSlaveConfig("fakeId","fakePodDescription", podYaml,
            "fakeLabel");

        return podParams;

    }

    public Label getFakeLabel(String name) {
        Label fakeLabel = new Label(name) {
            @Override
            public String getExpression() {
                return null;
            }

            @Override
            public boolean matches(VariableResolver<Boolean> resolver) {
                return false;
            }

            @Override
            public <V, P> V accept(LabelVisitor<V, P> visitor, P param) {
                return null;
            }

            @Override
            public LabelOperatorPrecedence precedence() {
                return null;
            }
        };
        return fakeLabel;
    }

}