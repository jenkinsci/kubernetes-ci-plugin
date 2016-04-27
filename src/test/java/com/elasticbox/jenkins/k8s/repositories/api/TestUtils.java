package com.elasticbox.jenkins.k8s.repositories.api;

import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubContent;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubContentLinks;

/**
 * Created by serna on 4/20/16.
 */
public final class TestUtils {

    public static GitHubContent getFakeChartDetails(){
        GitHubContent chartDetail =  new GitHubContent();
        chartDetail.setName("Chart.yaml");
        chartDetail.setDownloadUrl("https://raw.githubusercontent.com/helm/charts/master/rabbitmq/Chart.yaml");
        chartDetail.setGitUrl("https://api.github.com/repos/helm/charts/git/blobs/95ec050bd19616c09fbeca9ddabeff8e5824cbc4");
        chartDetail.setHtmlUrl("https://github.com/helm/charts/blob/master/rabbitmq/Chart.yaml");
        chartDetail.setGitHubContentLinks(new GitHubContentLinks());
        chartDetail.setPath("rabbitmq/Chart.yaml");
        chartDetail.setSha("95ec050bd19616c09fbeca9ddabeff8e5824cbc4");
        chartDetail.setType("file");
        chartDetail.setSize(234);
        chartDetail.setUrl("https://api.github.com/repos/helm/charts/contents/rabbitmq/Chart.yaml?ref=master");
        return chartDetail;
    }

    public static GitHubContent getFakeReadme(){
        GitHubContent readme =  new GitHubContent();
        readme.setName("README.md");
        readme.setPath("rabbitmq/README.md");
        readme.setSha("3405e06b74a677255c3ca3adae09a02450a5d5f5");
        readme.setSize(81);
        readme.setUrl("https://api.github.com/repos/helm/charts/contents/rabbitmq/README.md?ref=master");
        readme.setHtmlUrl("https://github.com/helm/charts/blob/master/rabbitmq/README.md");
        readme.setGitUrl("https://api.github.com/repos/helm/charts/git/blobs" +
            "/3405e06b74a677255c3ca3adae09a02450a5d5f5");
        readme.setDownloadUrl("https://raw.githubusercontent.com/helm/charts/master/rabbitmq/README.md");
        readme.setType("file");
        readme.setGitHubContentLinks(new GitHubContentLinks());
        return readme;
    }

    public static GitHubContent getFakeManifestsFolder(){
        GitHubContent manifestsFolder =  new GitHubContent();
        manifestsFolder.setName("manifests");
        manifestsFolder.setPath("rabbitmq/manifests");
        manifestsFolder.setSha("87c4ecb099308dc0c6058dbaf19f73f97f2c2282");
        manifestsFolder.setSize(0);
        manifestsFolder.setUrl("https://api.github.com/repos/helm/charts/contents/rabbitmq/manifests?ref=master");
        manifestsFolder.setHtmlUrl("https://github.com/helm/charts/tree/master/rabbitmq/manifests");
        manifestsFolder.setGitUrl("https://api.github.com/repos/helm/charts/git/trees/87c4ecb099308dc0c6058dbaf19f73f97f2c2282");
        manifestsFolder.setDownloadUrl(null);
        manifestsFolder.setType("dir");
        manifestsFolder.setGitHubContentLinks(new GitHubContentLinks());
        return manifestsFolder;
    }

    public static GitHubContent getFakeServiceManifest(){
        GitHubContent service =  new GitHubContent();
        service.setName("rabbitmq-svc.yaml");
        service.setPath("rabbitmq/manifests/rabbitmq-svc.yaml");
        service.setSha("63bfbe4c5ce561a4fbc3a7e3f52111dee52d90a8");
        service.setSize(173);
        service.setUrl("https://api.github.com/repos/helm/charts/contents/rabbitmq/manifests/rabbitmq-svc.yaml?ref=master");
        service.setHtmlUrl("https://github.com/helm/charts/blob/master/rabbitmq/manifests/rabbitmq-svc.yaml");
        service.setGitUrl("https://api.github.com/repos/helm/charts/git/blobs/63bfbe4c5ce561a4fbc3a7e3f52111dee52d90a8");
        service.setDownloadUrl("https://raw.githubusercontent.com/helm/charts/master/rabbitmq/manifests/rabbitmq-svc.yaml");
        service.setType("file");
        service.setGitHubContentLinks(new GitHubContentLinks());
        return service;
    }

    public static GitHubContent getFakeReplicationControllerManifest(){
        GitHubContent rc =  new GitHubContent();
        rc.setName("rabbitmq-rc.yaml");
        rc.setPath("rabbitmq/manifests/rabbitmq-rc.yaml");
        rc.setSha("af5769570942320a8bd018ae054280028dd5f0c9");
        rc.setSize(335);
        rc.setUrl("https://api.github.com/repos/helm/charts/contents/rabbitmq/manifests/rabbitmq-rc.yaml?ref=master");
        rc.setHtmlUrl("https://github.com/helm/charts/blob/master/rabbitmq/manifests/rabbitmq-rc.yaml");
        rc.setGitUrl("https://api.github.com/repos/helm/charts/git/blobs/af5769570942320a8bd018ae054280028dd5f0c9");
        rc.setDownloadUrl("https://raw.githubusercontent.com/helm/charts/master/rabbitmq/manifests/rabbitmq-rc.yaml");
        rc.setType("file");
        rc.setGitHubContentLinks(new GitHubContentLinks());
        return rc;
    }

}
