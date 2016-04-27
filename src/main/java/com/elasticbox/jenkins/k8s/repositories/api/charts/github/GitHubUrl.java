package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by serna on 4/14/16.
 */
public class GitHubUrl {

    private String baseUrl;
    private URL parsedUrl;

    public GitHubUrl(String url)  {
        this.baseUrl =  normalize(url);
        try {
            this.parsedUrl = new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw  new RuntimeException("Malformed URL: " + url);
        }
    }

    private static String normalize(String url) {

        if (StringUtils.isBlank(url)) {
            return null;
        }
        // Strip "/tree/..."
        if (url.contains("/tree/")) {
            url = url.replaceFirst("/tree/.*$", "");
        }
        if (!url.endsWith("/")) {
            url += '/';
        }
        return url;
    }

    public String protocol() {
        return parsedUrl.getProtocol();
    }

    public String host() {
        return parsedUrl.getHost();
    }


    public String owner() {
        final String[] split = parsedUrl.getPath().split("/");
        if (split.length >= 1 ) {
            return split[1];
        }
        return null;
    }

    public String repo() {
        final String[] split = parsedUrl.getPath().split("/");
        if (split.length >= 2 ) {
            return split[2];
        }
        return null;
    }

    public String path() {
        final String path = parsedUrl.getPath();
        if (path.charAt(path.length() - 1) == '/') {
            return path.substring(1,path.length() - 1);
        }
        return path.substring(1);
    }

    public String [] pathAsArray() {
        final String[] split = parsedUrl.getPath().split("/");
        return Arrays.copyOfRange(split,1,split.length - 1);
    }

    public String query() {
        final String query = this.parsedUrl.getQuery();
        if (query != null && query.charAt(query.length() - 1) == '/') {
            return query.substring(0,query.length() - 1);
        }
        return query;

    }


    public String commitId(final String id) {
        return new StringBuilder().append(baseUrl).append("commit/").append(id).toString();
    }

    @Override
    public String toString() {
        return this.baseUrl;
    }

}
