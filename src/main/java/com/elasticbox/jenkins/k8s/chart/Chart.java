package com.elasticbox.jenkins.k8s.chart;

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
        this.version = builder.chartDetails.getDescription();
        this.description = builder.chartDetails.getDescription();

        final List<String> mantainers = builder.chartDetails.getMantainers();
        this.maintainers = mantainers.toArray(new String[mantainers.size()]);

        this.pods = builder.pods.toArray(new Pod[builder.pods.size()]);

        this.services = builder.services.toArray(new Service[builder.services.size()]);

        this.replicationControllers = builder.replicationControllers.toArray(
            new ReplicationController[builder.replicationControllers.size()]);

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

        ChartDetails chartDetails;

        private List<Service> services = new ArrayList<>();
        private List<ReplicationController> replicationControllers =  new ArrayList<>();
        private List<Pod> pods = new ArrayList<>();

        public ChartBuilder chartDetails(ChartDetails details) {
            this.chartDetails = details;
            return this;
        }

        public ChartBuilder addService (Service service) {
            this.services.add(service);
            return this;
        }

        public ChartBuilder addPod (Pod pod) {
            this.pods.add(pod);
            return this;
        }

        public ChartBuilder addReplicationController (ReplicationController controller) {
            this.replicationControllers.add(controller);
            return this;
        }

        public Chart build () {
            return new Chart(this);
        }

    }
}

