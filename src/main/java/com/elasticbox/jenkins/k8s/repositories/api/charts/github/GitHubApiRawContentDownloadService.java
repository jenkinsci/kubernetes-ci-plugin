package com.elasticbox.jenkins.k8s.repositories.api.charts.github;

import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by serna on 4/26/16.
 */
public interface GitHubApiRawContentDownloadService {

    @GET
    Observable<String> rawContent(@Url String url);

}
