package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class GitHubContentLinks {

    @SerializedName("self")
    @Expose
    private String self;
    @SerializedName("git")
    @Expose
    private String git;
    @SerializedName("html")
    @Expose
    private String html;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getGit() {
        return git;
    }

    public void setGit(String git) {
        this.git = git;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

}

