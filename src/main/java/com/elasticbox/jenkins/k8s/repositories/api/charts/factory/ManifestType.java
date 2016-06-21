/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.repositories.api.charts.factory;


/**
 * Created by serna on 4/18/16.
 *
 */
public enum ManifestType {

    POD("Pod"),
    REPLICATION_CONTROLLER("ReplicationController"),
    SERVICE("Service"),
    NOT_RECOGNIZED("NotSupportedYet");

    private String kind;

    ManifestType(String kind) {
        this.kind = kind;
    }

    public String getKind() {
        return kind;
    }

    public static boolean oneOf(String kind) {
        for (ManifestType type: ManifestType.values()) {
            if (type.getKind().equals(kind)) {
                return true;
            }
        }
        return  false;
    }

    public static ManifestType findByType(String kind) {

        if (kind == null || kind.equals("")) {
            return NOT_RECOGNIZED;
        }

        for (ManifestType type: ManifestType.values()) {
            if (type.getKind().equals(kind)) {
                return type;
            }
        }

        return NOT_RECOGNIZED;
    }
}

