package com.elasticbox.jenkins.k8s.cfg;

import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.util.TestLogHandler;
import com.elasticbox.jenkins.k8s.util.TestUtils;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.slaves.Cloud;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TestDiscoverLocalKubernetesCloud {

    private static final String FAKE_IP = "FAKE_IP";
    private static final String FAKE_PORT = "FAKE_PORT";
    private static final String KUBERNETES_PORT_443_TCP_ADDR = "KUBERNETES_PORT_443_TCP_ADDR";
    private static final String KUBERNETES_PORT_443_TCP_PORT = "KUBERNETES_PORT_443_TCP_PORT";

    private static final String LOG_MESSAGE_NOT_FOUND = " <-- Log message not found";

    private static KubernetesRepository kubernetesRepositoryMock = Mockito.mock(KubernetesRepository.class);

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    private static TestLogHandler testLogHandler = new TestLogHandler();

    @BeforeClass
    public static void initEnv() {
        final Map<String, String> newEnv = new HashMap<>();
        newEnv.put(KUBERNETES_PORT_443_TCP_ADDR, FAKE_IP);
        newEnv.put(KUBERNETES_PORT_443_TCP_PORT, FAKE_PORT);

        TestUtils.setEnv(newEnv);
    }

    @Initializer(after = InitMilestone.EXTENSIONS_AUGMENTED)
    public static void initTestBeforePluginInitializer() throws Exception {
        testLogHandler.clear();

        Logger logger = Logger.getLogger("com.elasticbox.jenkins.k8s");
        logger.setUseParentHandlers(false);
        logger.addHandler(testLogHandler);

        Mockito.when(kubernetesRepositoryMock.testConnection(Mockito.anyString() )).thenReturn(true);
        PluginInitializer.kubeRepository = kubernetesRepositoryMock;
    }

    @Test
    public void testAutoDiscoverCloud() {

        // Testing first PluginInitializer run that should detect and add the local cloud:
        Cloud cloud = jenkins.getInstance().getCloud(PluginInitializer.LOCAL_CLOUD_NAME);
        Assert.assertNotNull("Local Kubernetes cloud not found", cloud);

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
