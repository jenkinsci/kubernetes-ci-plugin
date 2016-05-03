package com.elasticbox.jenkins.k8s.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.PodRepository;
import com.elasticbox.jenkins.k8s.repositories.ReplicationControllerRepository;
import com.elasticbox.jenkins.k8s.repositories.ServiceRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;

import java.util.logging.Logger;

@Singleton
public class ChartDeploymentServiceImpl implements ChartDeploymentService {

    private static final Logger LOGGER = Logger.getLogger(ChartDeploymentServiceImpl.class.getName() );

    private final KubernetesRepository kubernetesRepository;
    private final ChartRepository chartRepository;
    private final ServiceRepository serviceRepository;
    private final PodRepository podRepository;
    private final ReplicationControllerRepository replicationControllerRepository;

    @Inject
    public ChartDeploymentServiceImpl(KubernetesRepository kubernetesRepository, ChartRepository chartRepository,
                                      ServiceRepository serviceRepository, PodRepository podRepository,
                                      ReplicationControllerRepository replicationControllerRepository) {

        this.kubernetesRepository = kubernetesRepository;
        this.chartRepository = chartRepository;
        this.serviceRepository = serviceRepository;
        this.podRepository = podRepository;
        this.replicationControllerRepository = replicationControllerRepository;
    }

    @Override
    public void deployChart(String kubeName, String namespace, ChartRepo chartRepo, String chartName)
            throws ServiceException {

        try {
            final Chart chart = chartRepository.chart(chartRepo, chartName);

            if (!kubernetesRepository.namespaceExists(kubeName, namespace)) {
                LOGGER.warning("Namespace not found, creating it: " + namespace);
                kubernetesRepository.createNamespece(kubeName, namespace);
            }

            if (chart.getServices() != null) {
                for (Service service : chart.getServices()) {
                    serviceRepository.create(kubeName, namespace, service);
                }
            }

            if (chart.getReplicationControllers() != null) {
                for (ReplicationController replicationController : chart.getReplicationControllers()) {
                    replicationControllerRepository.create(kubeName, namespace, replicationController);
                }
            }

            if (chart.getPods() != null) {
                for (Pod pod : chart.getPods()) {
                    podRepository.create(kubeName, namespace, pod);
                }
            }

        } catch (RepositoryException exception) {
            LOGGER.severe("Error accessing/creating namespace [" + namespace + "]: " + exception.getMessage() );
            throw  new ServiceException(exception);
        }

    }


}
