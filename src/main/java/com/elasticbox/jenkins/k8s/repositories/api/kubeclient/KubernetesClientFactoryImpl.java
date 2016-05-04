package com.elasticbox.jenkins.k8s.repositories.api.kubeclient;

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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class KubernetesClientFactoryImpl implements KubernetesClientFactory {

    private static final Logger LOGGER = Logger.getLogger(KubernetesClientFactoryImpl.class.getName() );

    private Map<String, KubernetesClient> cloudClients = new HashMap<>();

    @Override
    public synchronized KubernetesClient getKubernetesClient(String kubeName) throws RepositoryException {

        if (cloudClients.containsKey(kubeName)) {
            return cloudClients.get(kubeName);
        }

        final KubernetesClient kubernetesClient = createKubernetesClient(kubeName);
        cloudClients.put(kubeName, kubernetesClient);

        return kubernetesClient;
    }

    private KubernetesClient createKubernetesClient(String kubeName) throws RepositoryException {
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
        cloudClients.remove(kubeName);
    }
}
