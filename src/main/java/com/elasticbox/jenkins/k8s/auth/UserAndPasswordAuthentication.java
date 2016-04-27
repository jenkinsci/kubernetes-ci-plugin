package com.elasticbox.jenkins.k8s.auth;

public  class UserAndPasswordAuthentication implements Authentication {
    private String user;
    private String password;

    public UserAndPasswordAuthentication(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}