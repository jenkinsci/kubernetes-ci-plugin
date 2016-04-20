package com.elasticbox.jenkins.k8s.chart;

import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by serna on 4/15/16.
 */
public class Chart {

    private String details;
    private String name;
    private String home;
    private String source;
    private String version;
    private String description;
    private String [] maintainers;

    private Service [] services;
    private ReplicationController [] replicationControllers;
    private Pod [] pods;

    private Chart(ChartBuilder builder) {
        this.details = builder.chartDetails.getDetails();
        this.name = builder.chartDetails.getName();
        this.home = builder.chartDetails.getHome();
        this.source = builder.chartDetails.getSource();
        this.version = builder.chartDetails.getVersion();
        this.description = builder.chartDetails.getDescription();

        final List<String> mantainers = builder.chartDetails.getMaintainers();
        if (!mantainers.isEmpty()) {
            this.maintainers = mantainers.toArray(new String[mantainers.size()]);
        }

        if (!builder.pods.isEmpty()) {
            this.pods = builder.pods.toArray(new Pod[builder.pods.size()]);
        }

        if (!builder.services.isEmpty()) {
            this.services = builder.services.toArray(new Service[builder.services.size()]);
        }

        if (!builder.replicationControllers.isEmpty()) {
            this.replicationControllers = builder.replicationControllers.toArray(
                new ReplicationController[builder.replicationControllers.size()]);
        }


    }

    public String getDetails() {
        return details;
    }

    public String getName() {
        return name;
    }

    public String getHome() {
        return home;
    }

    public String getSource() {
        return source;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String[] getMaintainers() {
        return maintainers;
    }

    public Service[] getServices() {
        return services;
    }

    public ReplicationController[] getReplicationControllers() {
        return replicationControllers;
    }

    public Pod[] getPods() {
        return pods;
    }

    public static class ChartBuilder {

        private List<RepositoryException> errors = new ArrayList<>();

        ChartDetails chartDetails;

        private List<Service> services = new ArrayList<>();
        private List<ReplicationController> replicationControllers =  new ArrayList<>();
        private List<Pod> pods = new ArrayList<>();

        public ChartBuilder chartDetails(ChartDetails details) {
            this.chartDetails = details;
            return this;
        }

        public ChartBuilder addService(Service service) {
            this.services.add(service);
            return this;
        }

        public ChartBuilder addPod(Pod pod) {
            this.pods.add(pod);
            return this;
        }

        public ChartBuilder addReplicationController(ReplicationController controller) {
            this.replicationControllers.add(controller);
            return this;
        }

        public Chart build() throws RepositoryException {
            if (errors.isEmpty()) {
                return new Chart(this);
            }

            throw errors.get(0);
        }

        public ChartBuilder addError(RepositoryException exception) {
            this.errors.add(exception);
            return this;
        }
    }
}

