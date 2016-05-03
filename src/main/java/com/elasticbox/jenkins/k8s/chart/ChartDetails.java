package com.elasticbox.jenkins.k8s.chart;

import java.util.Collections;
import java.util.List;

/**
 * Created by serna on 4/19/16.
 */
public class ChartDetails {

    private String details;
    private String name;
    private String home;
    private String source;
    private String version;
    private String description;

    private List<String> maintainers;

    public ChartDetails() {
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getMaintainers() {
        return Collections.unmodifiableList(this.maintainers);
    }

    public void setMaintainers(List<String> maintainers) {
        this.maintainers = maintainers;
    }
}
