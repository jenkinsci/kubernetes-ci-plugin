package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Links {

    @SerializedName("self")
    @Expose
    private String self;
    @SerializedName("git")
    @Expose
    private String git;
    @SerializedName("html")
    @Expose
    private String html;

    /**
     *
     * @return
     * The self
     */
    public String getSelf() {
        return self;
    }

    /**
     *
     * @param self
     * The self
     */
    public void setSelf(String self) {
        this.self = self;
    }

    /**
     *
     * @return
     * The git
     */
    public String getGit() {
        return git;
    }

    /**
     *
     * @param git
     * The git
     */
    public void setGit(String git) {
        this.git = git;
    }

    /**
     *
     * @return
     * The html
     */
    public String getHtml() {
        return html;
    }

    /**
     *
     * @param html
     * The html
     */
    public void setHtml(String html) {
        this.html = html;
    }

}

