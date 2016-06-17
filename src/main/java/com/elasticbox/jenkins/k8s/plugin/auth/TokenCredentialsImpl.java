/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.plugin.auth;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.elasticbox.jenkins.k8s.plugin.auth.TokenCredentials;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class TokenCredentialsImpl extends BaseStandardCredentials implements TokenCredentials {
    @Nonnull
    private final Secret token;

    @DataBoundConstructor
    public TokenCredentialsImpl(@CheckForNull CredentialsScope scope, @CheckForNull String id,
                                @CheckForNull String description, @Nonnull Secret token) {

        super(scope, id, description);
        this.token = token;
    }

    public Secret getSecret() {
        return this.token;
    }

    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {

        private static final String DISPLAY_NAME = "Authentication Token";

        public DescriptorImpl() {
        }

        public String getDisplayName() {
            return DISPLAY_NAME;
        }
    }

}