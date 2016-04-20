package com.elasticbox.jenkins.k8s.repositories.api.charts;

import com.elasticbox.jenkins.k8s.chart.Chart;
import com.elasticbox.jenkins.k8s.chart.ChartDetails;
import com.elasticbox.jenkins.k8s.repositories.ChartRepository;
import com.elasticbox.jenkins.k8s.repositories.api.charts.factory.ManifestFactory;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GithubContent;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GithubRawContentService;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GithubService;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GithubUrl;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;

import hudson.Extension;

import org.yaml.snakeyaml.Yaml;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by serna on 4/13/16.
 */
@Extension
public class ChartRepositoryApiImpl implements ChartRepository {

    private GithubService githubService;
    private GithubRawContentService githubRawContentService;

    public ChartRepositoryApiImpl() {

        githubService = new Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()
            .create(GithubService.class);


        githubRawContentService = new Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()
            .create(GithubRawContentService.class);

    }

    @Override
    public List<String> chartNames(String repo, String ref) throws RepositoryException {

        GithubUrl repoUrl = new GithubUrl(repo);

        final List<String> chartNames = new ArrayList<>();

        String defaultRef = (ref == null || ref.equals("")) ? "master" : ref;

        githubService.contents(repoUrl.owner(), repoUrl.repo(), "", defaultRef)
            .flatMap(new Func1<List<GithubContent>, Observable<GithubContent>>() {
                @Override
                public Observable<GithubContent> call(List<GithubContent> gitHubContents) {
                    return Observable.from(gitHubContents);
                }
            })
            .filter(new Func1<GithubContent, Boolean>() {
                @Override
                public Boolean call(GithubContent gitHubContent) {
                    return gitHubContent.getType().equals("dir");
                }
            })
            .subscribe(new Action1<GithubContent>() {
                @Override
                public void call(GithubContent gitHubContent) {
                    chartNames.add(gitHubContent.getName());
                }
            });

        return chartNames;

    }

    public Chart chart(String repo, String name) throws RepositoryException {
        return chart(repo, name, null);
    }

    //TODO study how to do that using an specific worker
    public Chart chart(String repo, String name, String ref) throws RepositoryException {

        final Chart.ChartBuilder chartBuilder = new Chart.ChartBuilder();

        GithubUrl repoUrl = new GithubUrl(repo);

        final String defaultRef = (ref == null || ref.equals("")) ? "master" : ref;


        githubService.contents(repoUrl.owner(), repoUrl.repo(), name , defaultRef)
            .flatMap(new Func1<List<GithubContent>, Observable<GithubContent>>() {
                @Override
                public Observable<GithubContent> call(List<GithubContent> gitHubContents) {
                    return Observable.from(gitHubContents);
                }
            })
            .subscribe(new Action1<GithubContent>() {
                @Override
                public void call(GithubContent gitHubContent) {

                    if (gitHubContent.getName().equals("Chart.yaml")) {
                        final String chartUrl = gitHubContent.getDownloadUrl();
                        //add the general details to the chart
                        chartDetails(chartUrl, chartBuilder);
                    } else if (gitHubContent.getType().equals("dir")
                                && gitHubContent.getName().equals("manifests")) {
                            //retrieve the contained yaml files
                            manifests(gitHubContent.getUrl(), chartBuilder);
                        }
                }
            });

        return chartBuilder.build();

    }

    private void manifests(String url, final Chart.ChartBuilder chartBuilder) {

        githubService.contentsFromUrl(url)
            .flatMap(new Func1<List<GithubContent>, Observable<GithubContent>>() {
                @Override
                public Observable<GithubContent> call(List<GithubContent> gitHubContents) {
                    return Observable.from(gitHubContents);
                }
            })
            .subscribe(new Action1<GithubContent>() {
                @Override
                public void call(GithubContent githubContent) {
                    final String manifestUrl = githubContent.getDownloadUrl();
                    manifest(manifestUrl, chartBuilder);
                }
            });


    }

    private void manifest(String url, final Chart.ChartBuilder chartBuilder) {

        githubRawContentService.rawContentFromUrl(url).subscribe(
            new Action1<String>() {
                @Override
                public void call(String yaml) {
                    try {
                        ManifestFactory.addManifest(yaml, chartBuilder);
                    } catch (RepositoryException e) {
                        chartBuilder.addError(e);
                    }
                }
            }
        );

    }


    private <T> void chartDetails(String url, final Chart.ChartBuilder chartBuilder) {

        final Yaml yaml = new Yaml();
        final Observable<ChartDetails> observable = githubRawContentService.rawContentFromUrl(url)
            .map(new Func1<String, ChartDetails>() {
                @Override
                public ChartDetails call(String yamlString) {
                    final ChartDetails chartDetails = yaml.loadAs(yamlString, ChartDetails.class);
                    return chartDetails;
                }
            });
        final Subscription subscription = observable.subscribe(new Action1<ChartDetails>() {
            @Override
            public void call(ChartDetails details) {
                chartBuilder.chartDetails(details);
            }
        });

    }

    public GithubService getGithubService() {
        return githubService;
    }

    public void setGithubService(GithubService githubService) {
        this.githubService = githubService;
    }

    public GithubRawContentService getGithubRawContentService() {
        return githubRawContentService;
    }

    public void setGithubRawContentService(GithubRawContentService githubRawContentService) {
        this.githubRawContentService = githubRawContentService;
    }
}
