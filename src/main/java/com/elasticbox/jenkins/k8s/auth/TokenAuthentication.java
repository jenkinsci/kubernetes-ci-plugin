package com.elasticbox.jenkins.k8s.auth;

/**
 * Created by serna on 4/27/16.
 */
public class TokenAuthentication implements Authentication {
    private String authToken;

    public TokenAuthentication(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    @Override
    public String getKey() {
        return "Token:" + authToken;
    }
}
