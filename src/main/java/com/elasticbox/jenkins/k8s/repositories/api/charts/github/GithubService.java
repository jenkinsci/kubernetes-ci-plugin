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
public interface GithubService {


    @GET
    Observable<List<GithubContent>> contentsFromUrl(@Url String url);

    @GET("/repos/{owner}/{repo}/contents/{path}")
    Observable<List<GithubContent>> contents(
        @Path("owner") String owner,
        @Path("repo") String repo,
        @Path("path") String path,
        @Query("ref") String ref);

    @GET("/{path}")
    Observable<List<GithubContent>> contents(
        @Path("path") String path,
        @Query("ref") String ref);


}
