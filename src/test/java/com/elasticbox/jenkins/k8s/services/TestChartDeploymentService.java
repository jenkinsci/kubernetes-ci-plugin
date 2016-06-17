/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.services;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.util.KeyValuePair;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.ReplicationControllerRepository;
import com.elasticbox.jenkins.k8s.repositories.ServiceRepository;
import com.elasticbox.jenkins.k8s.util.TestUtils;
import com.elasticbox.jenkins.k8s.repositories.api.charts.ChartRepositoryApiImpl;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class TestChartDeploymentService {

    private static final String TEST_NAMESPACE = "test-chart";
    private static final String SERVER_URL = "http://10.107.56.115:8080/";
    private static final String KIND_SERVICE = "Service";
    private static final String KIND_RC = "ReplicationController";

    private InputStream nginxSvcIS;
    private InputStream nginxRcIS;

    private Service nginxSvc;
    private ReplicationController nginxRc;
    private KubernetesClient client;

    private KubernetesRepository kubernetesRepositoryMock = Mockito.mock(KubernetesRepository.class);
    private ChartRepositoryApiImpl chartRepositoryMock = new ChartRepositoryApiImpl();
    private ServiceRepository serviceRepositoryMock = Mockito.mock(ServiceRepository.class);
    private PodRepository podRepositoryMock = Mockito.mock(PodRepository.class);
    private ReplicationControllerRepository rcRepositoryMock = Mockito.mock(ReplicationControllerRepository.class);

    @Before
    public void setUpMocks() throws IOException, RepositoryException {
        chartRepositoryMock.setClientsFactory(TestUtils.getGitHubClientsFactoryMock() );

        Mockito.when(kubernetesRepositoryMock.namespaceExists(anyString(), anyString() )).thenReturn(Boolean.TRUE);

        Mockito.doNothing().when(serviceRepositoryMock).create(anyString(), anyString(), any(Service.class) );
        Mockito.doNothing().when(podRepositoryMock).create(anyString(), anyString(), any(Pod.class) );
        Mockito.doNothing().when(rcRepositoryMock).create(anyString(), anyString(), any(ReplicationController.class));
    }
    
    @Test
    public void testDeployChart() throws Exception {

        ChartDeploymentService service = new ChartDeploymentServiceImpl(kubernetesRepositoryMock, chartRepositoryMock,
                serviceRepositoryMock, podRepositoryMock, rcRepositoryMock);

        ChartRepo fakeRepo = new ChartRepo("https://github.com/fakeOwner/fakeChartsRepo");

        service.deployChart("fakeKubeCloud", "fakeNamespace", fakeRepo, "fakeChartName",
                Collections.singletonMap("fakeLabelKey", "fakeLabelText") );

        Mockito.verify(serviceRepositoryMock).create(anyString(), anyString(), any(Service.class), any(Map.class) );

        Mockito.verify(rcRepositoryMock).create(
                anyString(), anyString(), any(ReplicationController.class), any(Map.class) );

        Mockito.verify(podRepositoryMock, Mockito.never() ).create(
                anyString(), anyString(), any(Pod.class), any(Map.class) );

        Mockito.verify(kubernetesRepositoryMock, Mockito.never() )
                .createNamespece(anyString(), anyString(), any(KeyValuePair.class) );
    }

}