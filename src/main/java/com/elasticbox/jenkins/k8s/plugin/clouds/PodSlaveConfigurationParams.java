/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.plugin.clouds;

import org.apache.commons.lang.StringUtils;

public class PodSlaveConfigurationParams {

    final String description;
    final String podYaml;
    final String labels;

    public PodSlaveConfigurationParams(String description, String podYaml, String label) {
        this.description = description;
        this.podYaml = podYaml;
        this.labels = label;
    }

    public String getDescription() {
        return description;
    }

    public String getPodYaml() {
        return podYaml;
    }

    public String [] getLabels() {
        return StringUtils.split(labels, " ,");
    }

    public String getLabelsAsString() {
        return labels;
    }

    @Override
    public String toString() {
        return "PodSlaveConfigurationParams{description='" + description + "', labels='" + labels + "'}";
    }
}