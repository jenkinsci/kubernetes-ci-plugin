package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

import java.util.List;

public interface GitHubApiContentsService {

    @GET
    Observable<List<GitHubContent>> content(@Url String url);

    @GET("/repos/{ownerInCaseOfRepoUrl}/{repoInCaseOfRepoUrl}/contents/{path}")
    Observable<List<GitHubContent>> content(@Path("ownerInCaseOfRepoUrl") String owner,
                                            @Path("repoInCaseOfRepoUrl") String repo,
                                            @Path("path") String relativeToRepoPath,
                                            @Query("ref") String ref);


}
