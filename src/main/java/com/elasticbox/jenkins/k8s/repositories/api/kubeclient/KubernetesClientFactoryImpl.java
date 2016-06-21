/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.repositories.api.kubeclient;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Singleton;

import com.elasticbox.jenkins.k8s.auth.Authentication;
import com.elasticbox.jenkins.k8s.auth.TokenAuthentication;
import com.elasticbox.jenkins.k8s.auth.UserAndPasswordAuthentication;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloud;
import com.elasticbox.jenkins.k8s.plugin.clouds.KubernetesCloudParams;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import hudson.slaves.Cloud;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class KubernetesClientFactoryImpl
        extends CacheLoader<String, KubernetesClient>
        implements KubernetesClientFactory {

    private static final Logger LOGGER = Logger.getLogger(KubernetesClientFactoryImpl.class.getName() );

    public static final int MAX_NUM_CLIENTS_CACHED = 100;
    public static final int MAX_IDLE_HOURS = 24;

    private LoadingCache<String, KubernetesClient> cache = CacheBuilder.newBuilder()
            .maximumSize(MAX_NUM_CLIENTS_CACHED)
            .expireAfterAccess(MAX_IDLE_HOURS, TimeUnit.HOURS)
            .build(this);

    @Override
    public KubernetesClient getKubernetesClient(String kubeName) throws RepositoryException {
        try {
            return cache.get(kubeName);
        } catch (ExecutionException exception) {
            throw new RepositoryException("Error while creating client", exception);
        }
    }

    @Override
    public KubernetesClient load(String kubeName) throws Exception {

        final Jenkins instance = Jenkins.getInstance();

        final Cloud cloud = (instance != null) ? instance.getCloud(kubeName) : null;

        if (cloud != null && cloud instanceof KubernetesCloud) {
            KubernetesCloud kubeCloud = (KubernetesCloud) cloud;

            return createKubernetesClient(kubeCloud.getKubernetesCloudParams() );
        }

        String msg = "There is no KubernetesCloud with name: " + kubeName;
        LOGGER.severe(msg);

        throw new RepositoryException(msg);
    }

    @Override
    public KubernetesClient createKubernetesClient(KubernetesCloudParams kubeCloudParams) {
        ConfigBuilder builder = new ConfigBuilder().withMasterUrl(kubeCloudParams.getEndpointUrl() );
        Authentication authData = kubeCloudParams.getAuthData();

        if (authData != null) {
            if (authData instanceof TokenAuthentication) {
                builder.withOauthToken( ((TokenAuthentication)authData).getAuthToken() );

            } else if (authData instanceof UserAndPasswordAuthentication) {
                builder.withUsername( ((UserAndPasswordAuthentication) authData).getUser() );
                builder.withPassword( ((UserAndPasswordAuthentication) authData).getPassword() );
            }
        }

        if (kubeCloudParams.isDisableCertCheck() ) {
            builder.withTrustCerts(true);
        } else if (StringUtils.isNotEmpty(kubeCloudParams.getServerCert() )) {
            builder.withCaCertData(kubeCloudParams.getServerCert() );
        }

        if (LOGGER.isLoggable(Level.CONFIG) ) {
            LOGGER.config("Creating Kubernetes client: " + kubeCloudParams.getEndpointUrl() );
        }
        return new DefaultKubernetesClient(builder.build() );
    }

    @Override
    public void resetKubernetesClient(String kubeName) {
        cache.invalidate(kubeName);
    }
}
