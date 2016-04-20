package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by serna on 4/14/16.
 */
public class GithubUrl {

    private String baseUrl;
    private URI parsedURL;

    public GithubUrl(String url)  {
        this.baseUrl =  normalize(url);
        try {
            this.parsedURL = new URI(baseUrl);
        } catch (URISyntaxException e) {
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

    /**
     * Returns the owner of the repository
     * @return String according GithubAPI v3
     * (e.g. helm for https://api.github.com/repos/helm/charts/git/blobs/af5769570942320a8bd018ae054280028dd5f0c9)
     */
    public String owner(){
        final String[] split = parsedURL.getPath().split("/");
        if (split.length >= 1 ) {
            return split[1];
        }
        return null;
    }

    /**
     * Returns the repository
     * @return String according GithubAPI v3
     * (e.g. charts for https://api.github.com/repos/helm/charts/git/blobs/af5769570942320a8bd018ae054280028dd5f0c9)
     */
    public String repo(){
        final String[] split = parsedURL.getPath().split("/");
        if (split.length >= 2 ) {
            return split[2];
        }
        return null;
    }

    public String path(){
        final String path = parsedURL.getPath();
        if (path.charAt(path.length() - 1) == '/') {
            return path.substring(1,path.length()-1);
        }
        return path.substring(1);
    }

    public String query(){
        final String query = this.parsedURL.getQuery();
        if (query != null && query.charAt(query.length() - 1) == '/') {
            return query.substring(0,query.length()-1);
        }
        return query;

    }


    /**
     * Returns the URL to a particular commit.
     * @param id - the git SHA1 hash
     * @return URL String (e.g. http://github.com/juretta/github-plugin/commit/5e31203faea681c41577b685818a361089fac1fc)
     */
    public String commitId(final String id) {
        return new StringBuilder().append(baseUrl).append("commit/").append(id).toString();
    }

    @Override
    public String toString() {
        return this.baseUrl;
    }

}
