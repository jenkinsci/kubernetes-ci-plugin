/*
 * Copyright 2016 ElasticBox
 *
 * Licensed under the Apache License, Version 2.0, <LICENSE-APACHE or http://apache.org/licenses/LICENSE-2.0>
 * or the MIT license <LICENSE-MIT or http://opensource.org/licenses/MIT> , at your option.
 * This file may not be copied, modified, or distributed except according to those terms.
 */

package com.elasticbox.jenkins.k8s.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.elasticbox.jenkins.k8s.chart.ChartRepo;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubApiContentsService;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubApiRawContentDownloadService;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubApiResponseContentType;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubClientsFactory;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubContent;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubContentLinks;
import com.elasticbox.jenkins.k8s.repositories.error.RepositoryException;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import rx.Observable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public static GitHubClientsFactory getGitHubClientsFactoryMock() throws IOException, RepositoryException {
        final String chart = IOUtils.toString(TestUtils.class.getResourceAsStream("chartYaml.yaml") );
        final String service = IOUtils.toString(TestUtils.class.getResourceAsStream("serviceChartManifest.yaml") );
        final String rc = IOUtils.toString(TestUtils.class.getResourceAsStream("replicationControllerChartManifest.yaml") );

        // fake content of: https://api.github.com/repositories/44991456/contents/rabbitmq
        final List<GitHubContent> rootChartContent = Arrays.asList(
                TestUtils.getFakeChartDetails(),
                TestUtils.getFakeReadme(),
                TestUtils.getFakeManifestsFolder()
        );

        // fake content of: https://api.github.com/repos/helm/charts/contents/rabbitmq/manifests
        final List<GitHubContent> manifestsContent = Arrays.asList(
                TestUtils.getFakeReplicationControllerManifest(),
                TestUtils.getFakeServiceManifest()
        );

        final GitHubApiRawContentDownloadService gitHubApiRawContentDownloadService = Mockito.mock(GitHubApiRawContentDownloadService.class);
        when(gitHubApiRawContentDownloadService.rawContent(any(String.class)))
                .thenReturn(Observable.just(chart))
                .thenReturn(Observable.just(rc))
                .thenReturn(Observable.just(service));

        final GitHubApiContentsService gitHubApiContentsService = Mockito.mock(GitHubApiContentsService.class);
        when(gitHubApiContentsService.content(any(String.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(Observable.just(rootChartContent));
        when(gitHubApiContentsService.content(any(String.class)))
                .thenReturn(Observable.just(manifestsContent));

        final GitHubClientsFactory mockedClientFactory = Mockito.mock(GitHubClientsFactory.class);
        when(mockedClientFactory.getClient(any(ChartRepo.class), eq(GitHubApiContentsService.class), eq
                (GitHubApiResponseContentType.JSON)))
                .thenReturn(gitHubApiContentsService);
        when(mockedClientFactory.getClient(any(ChartRepo.class), eq(GitHubApiRawContentDownloadService.class), eq
                (GitHubApiResponseContentType.RAW_STRING)))
                .thenReturn(gitHubApiRawContentDownloadService);
        return mockedClientFactory;
    }

    public static void setEnv(Map<String, String> newenv)
    {
        try
        {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        }
        catch (NoSuchFieldException e)
        {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for(Class cl : classes) {
                    if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.putAll(newenv);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void clearEnv(String... keys)
    {
        try
        {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            for (String key: keys) {
                env.remove(key);
            }
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            for (String key: keys) {
                cienv.remove(key);
            }
        }
        catch (NoSuchFieldException e)
        {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for(Class cl : classes) {
                    if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        for (String key: keys) {
                            map.remove(key);
                        }
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
