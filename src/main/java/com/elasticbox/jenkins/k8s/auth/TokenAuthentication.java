package com.elasticbox.jenkins.k8s.auth;

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
