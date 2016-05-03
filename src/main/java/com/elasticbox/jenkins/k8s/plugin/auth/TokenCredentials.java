package com.elasticbox.jenkins.k8s.plugin.auth;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.Util;
import hudson.util.Secret;

import javax.annotation.Nonnull;

@NameWith(TokenCredentials.NameProvider.class)
public interface TokenCredentials extends StandardCredentials {
    @Nonnull
    Secret getSecret();

    class NameProvider extends CredentialsNameProvider<TokenCredentials> {
        @Nonnull
        public String getName(TokenCredentials credentials) {
            String description = Util.fixEmptyAndTrim(credentials.getDescription() );
            return "Token ( " + (description != null ? description : credentials.getId() ) + ")";
        }
    }
}
