/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.cfg;

import static com.elasticbox.jenkins.k8s.cfg.PluginInitializer.JENKINS_SERVICE_HOST;
import static com.elasticbox.jenkins.k8s.cfg.PluginInitializer.JENKINS_SERVICE_PORT;
import static com.elasticbox.jenkins.k8s.cfg.PluginInitializer.KUBERNETES_API_SERVER_ADDR;
import static com.elasticbox.jenkins.k8s.cfg.PluginInitializer.KUBERNETES_API_SERVER_PORT;

import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.util.TestLogHandler;
import com.elasticbox.jenkins.k8s.util.TestUtils;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TestDiscoverLocalKubernetesCloud {

    private static final String FAKE_IP = "FAKE_IP";
    private static final String FAKE_PORT = "FAKE_PORT";

    private static final String LOG_MESSAGE_NOT_FOUND = " <-- Log message not found";

    private static KubernetesRepository kubernetesRepositoryMock = Mockito.mock(KubernetesRepository.class);

    private static File fileMock = Mockito.mock(File.class);

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    private static TestLogHandler testLogHandler = new TestLogHandler();

    @BeforeClass
    public static void initEnv() {

        final Map<String, String> newEnv = new HashMap<>();

        newEnv.put(KUBERNETES_API_SERVER_ADDR, FAKE_IP);
        newEnv.put(KUBERNETES_API_SERVER_PORT, FAKE_PORT);

        newEnv.put(JENKINS_SERVICE_HOST, FAKE_IP);
        newEnv.put(JENKINS_SERVICE_PORT, FAKE_PORT);

        TestUtils.setEnv(newEnv);
    }

    @AfterClass
    public static void restoreEnv() {

        TestUtils.clearEnv(KUBERNETES_API_SERVER_ADDR, KUBERNETES_API_SERVER_PORT,
                            JENKINS_SERVICE_HOST, JENKINS_SERVICE_PORT);
    }

    @Initializer(after = InitMilestone.EXTENSIONS_AUGMENTED)
    public static void initTestBeforePluginInitializer() throws Exception {
        testLogHandler.clear();

        Logger logger = Logger.getLogger("com.elasticbox.jenkins.k8s");
        logger.setUseParentHandlers(false);
        logger.addHandler(testLogHandler);

        Mockito.when(kubernetesRepositoryMock.testConnection(Mockito.anyString() )).thenReturn(true);
        PluginInitializer.kubeRepository = kubernetesRepositoryMock;
        PluginInitializer.KUBE_TOKEN_PATH = PluginInitializer.class.getResource("fakeToken").getFile();
    }

    @Test
    public void testAutoDiscoverCloud() {

        // Testing first PluginInitializer run that should detect and add the local cloud:
        KubernetesCloud cloud = (KubernetesCloud) jenkins.getInstance().getCloud(PluginInitializer.LOCAL_CLOUD_NAME);
        Assert.assertNotNull("Local Kubernetes cloud not found", cloud);

        Assert.assertNotNull("Default chart repository configuration not included", cloud.getChartRepositoryConfigurations() );
        Assert.assertEquals("Chart repository configuration list must have just one", cloud.getChartRepositoryConfigurations().size(), 1);
        Assert.assertTrue("No Kubernetes service token has been configured", StringUtils.isNotEmpty(cloud.getCredentialsId() ));
        Assert.assertEquals("No credentials object has been added", SystemCredentialsProvider.getInstance().getCredentials().size(), 1);

        Assert.assertNotNull("Default pod slave configuration not included", cloud.getPodSlaveConfigurations() );
        Assert.assertEquals("Pod slave configuration list must have just one", cloud.getPodSlaveConfigurations().size(), 1);
        Assert.assertNotNull("Default pod slave configuration yaml not loaded", cloud.getPodSlaveConfigurations().get(0).getPodYaml() );
        Assert.assertTrue("Jenkins addr. not injected in pod yaml.",
                cloud.getPodSlaveConfigurations().get(0).getPodYaml().contains(FAKE_IP) );

        assertLoggedMessage("Kubernetes Cloud found!");
        assertLoggedMessage("Adding local Kubernetes Cloud configuration");

        testLogHandler.clear();

        // Testing second PluginInitializer run that should detect the local cloud already configured:
        PluginInitializer.checkLocalKubernetesCloud();

        assertLoggedMessage("Kubernetes Cloud found!");
        assertLoggedMessage("Local Kubernetes Cloud already configured");
    }

    private void assertLoggedMessage(String message) {
        Assert.assertTrue(message + LOG_MESSAGE_NOT_FOUND, testLogHandler.isLogMessageFound(message) );
    }
}
