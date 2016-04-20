package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

import java.util.List;

/**
 * Created by serna on 4/14/16.
 */
public interface GithubRawContentService {

    @GET("/{owner}/{repo}/{ref}/{path}")
    Observable<String> raw(
        @Path("owner") String owner,
        @Path("repo") String repo,
        @Path("ref") String ref,
        @Path("path") String path);

    @GET("/{path}")
    Observable<String> raw(
        @Path(value = "path", encoded = true) String path,
        @Query("ref") String ref);

    @GET
    Observable<String> rawContentFromUrl(@Url String url);

}
