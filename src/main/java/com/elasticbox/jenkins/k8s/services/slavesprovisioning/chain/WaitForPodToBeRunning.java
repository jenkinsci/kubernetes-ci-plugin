package com.elasticbox.jenkins.k8s.services.slavesprovisioning.chain;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.repositories.KubernetesRepository;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import com.elasticbox.jenkins.k8s.services.error.ServiceException;
import com.elasticbox.jenkins.k8s.services.task.ScheduledPoolingTask;
import com.elasticbox.jenkins.k8s.services.task.TaskException;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class WaitForPodToBeRunning extends AbstractPodDeployment {

    private static final Logger LOGGER = Logger.getLogger(WaitForPodToBeRunning.class.getName());

    @Inject
    private KubernetesRepository kubernetesRepository;

    private static final long DELAY_SECONDS = 6;
    private static final long INITIAL_DELAY = 6;
    private static final long TIMEOUT = 60;

    /**
     * Its mission is wait for the Pod to be running until the specified timeout.
     */
    @Override
    public void handle(PodDeploymentContext deploymentContext) throws ServiceException {

        final String podName = deploymentContext.getPodToDeploy().getMetadata().getName();

        final KubernetesCloud cloudToDeployInto = deploymentContext.getCloudToDeployInto();

        final String namespace = cloudToDeployInto.getNamespace();

        try {

            final KubernetesClient client = kubernetesRepository.getClient(cloudToDeployInto.name);

            new WaitForThePodToBeRunningTask(client, namespace, podName, DELAY_SECONDS, INITIAL_DELAY, TIMEOUT)
                .execute();

            LOGGER.log(Level.INFO, "Pod is at the Running stage");

        } catch (TaskException error) {

            LOGGER.log(Level.SEVERE, "Error waiting for the Pod to be running", error);

            throw new ServiceException("Error waiting for the Pod to be running", error);

        } catch (RepositoryException error) {

            LOGGER.log(Level.SEVERE, "Error getting the client for the cloud " + cloudToDeployInto.name, error);

            throw new ServiceException("Error getting the client for the cloud " + cloudToDeployInto.name, error);
        }


    }

    public static enum PodState {

        PENDING("Pending"),
        RUNNING("Running"),
        SUCCEEDED("Succeeded"),
        FAILED("Failed"),
        UNKNOWN("Unknown");

        private String status;

        PodState(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public static PodState findByDescription(String status) {
            for (PodState state: PodState.values()) {
                if (state.getStatus().equals(status)) {
                    return state;
                }
            }
            return UNKNOWN;
        }

    }


    private static class WaitForThePodToBeRunningTask extends ScheduledPoolingTask<PodState> {

        private String namespace;
        private String podName;
        private KubernetesClient client;

        public WaitForThePodToBeRunningTask(KubernetesClient client,
                                            String namespace,
                                            String podName,
                                            long delay,
                                            long initialDelay,
                                            long timeout) {

            super(delay, initialDelay, timeout);

            this.client = client;
            this.podName = podName;
            this.namespace = namespace;
            this.result = PodState.UNKNOWN;
        }

        @Override
        protected void performExecute() throws TaskException {

            final Pod pod = client.pods().inNamespace(namespace).withName(podName).get();

            final String phase = pod.getStatus().getPhase();

            final PodState podStatus = PodState.findByDescription(phase);

            if (podStatus == PodState.FAILED) {

                LOGGER.log(Level.INFO, "Pod is at the Failed stage");

                throw new TaskException("Pod deployment failed");
            }

            this.result = podStatus;

            LOGGER.log(Level.INFO,
                "Pod: " + pod.getMetadata().getName() + " is at the " + result.getStatus() + "stage");
        }

        @Override
        public boolean isDone() {
            final PodState result = this.getResult();
            return result == PodState.RUNNING;
        }
    }



}
