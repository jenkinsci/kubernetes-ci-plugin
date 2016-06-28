/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

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
    private List<String> maintainers;

    private List<Service> services;
    private List<ReplicationController> replicationControllers;
    private List<Pod> pods;

    private Chart(ChartBuilder builder) {
        this.details = builder.chartDetails.getDetails();
        this.name = builder.chartDetails.getName();
        this.home = builder.chartDetails.getHome();
        this.source = builder.chartDetails.getSource();
        this.version = builder.chartDetails.getVersion();
        this.description = builder.chartDetails.getDescription();

        this.maintainers = builder.chartDetails.getMaintainers();
        this.pods = builder.pods;
        this.replicationControllers = builder.replicationControllers;
        this.services = builder.services;

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

    public List<String> getMaintainers() {
        return maintainers;
    }

    public List<Service> getServices() {
        return services;
    }

    public List<ReplicationController> getReplicationControllers() {
        return replicationControllers;
    }

    public List<Pod> getPods() {
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

