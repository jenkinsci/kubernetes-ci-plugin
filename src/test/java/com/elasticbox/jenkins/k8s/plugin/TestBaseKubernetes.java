package com.elasticbox.jenkins.k8s.plugin;

import com.elasticbox.jenkins.k8s.plugin.clouds.ChartRepositoryConfig;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloudParams;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

public class TestBaseKubernetes {

    protected static final String EMPTY = "";
    protected static final String FAKE_URL = "http://fake.url";
    protected static final String FAKE_NS = "fakeNs";
    protected static final String FAKE_CHARTS_REPO = "FakeChartsRepo";
    protected static final String FAKE_KUBE_CLOUD = "FakeKubeCloudName";
    protected static final String FAKE_MOCK_EXCEPTION = "FakeMockException";

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    protected ChartRepositoryConfig fakeChartRepoCfg;
    protected KubernetesCloud cloud;

    @Before
    public void setupCloud() throws Exception {
        fakeChartRepoCfg = new ChartRepositoryConfig(FAKE_CHARTS_REPO, FAKE_URL, EMPTY);
        cloud = new KubernetesCloud(EMPTY, "FakeKubeCloud", FAKE_URL, FAKE_NS, "10", EMPTY, true, EMPTY,
                                    Collections.singletonList(fakeChartRepoCfg), null);

        jenkins.getInstance().clouds.add(cloud);
    }

}
