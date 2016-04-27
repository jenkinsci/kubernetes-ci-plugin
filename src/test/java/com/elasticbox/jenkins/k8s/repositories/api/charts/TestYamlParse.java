package com.elasticbox.jenkins.k8s.repositories.api.charts;

import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubApiContentsService;
import com.elasticbox.jenkins.k8s.repositories.api.charts.github.GitHubContent;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.functions.Action1;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.List;

/**
 * Created by serna on 4/18/16.
 */
public class TestYamlParse {


    public static void main(String[] args) throws IOException {

        String baseUrl = "https:///git.elasticbox.com";
        String contentUrl = "https://git.elasticbox.com/api/v3/user/repos";

        String username = "serna@elasticbox.com";
        String password = "osern@Elastic1";



        if (username != null && password != null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            String credentials = username + ":" + password;
            final String basic = "Basic " + new BASE64Encoder().encode(credentials.getBytes());
            final OkHttpClient client = httpClient
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        final Request request = chain.request()
                            .newBuilder()
                            .addHeader("Authorization", basic)
                            .build();

                        final Response response = chain.proceed(request);
                        return response;
                    }
                })
                .addInterceptor(logging)
                .build();


            new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build()
                .create(GitHubApiContentsService.class)
                .content(contentUrl)
                .subscribe(new Action1<List<GitHubContent>>() {
                    @Override
                    public void call(List<GitHubContent> gitHubContents) {
                        for (GitHubContent gitHubContent : gitHubContents) {
                            System.out.println(gitHubContent.getName());
                        }
                    }
                });
        }

        baseUrl = "https://api.github.com";
        contentUrl = "https://api.github.com/repos/oserna/questions/contents";



        new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()
            .create(GitHubApiContentsService.class)
            .content(contentUrl)
            .subscribe(new Action1<List<GitHubContent>>() {
                @Override
                public void call(List<GitHubContent> gitHubContents) {
                    for (GitHubContent gitHubContent : gitHubContents) {
                        System.out.println(gitHubContent.getName());
                    }
                }
            });


    }


//    public static void main(String[] args) {
//
//        final Observable<String> someObservable = Observable.from(Arrays.asList(new Integer[]{2, 3, 5, 7, 11, 12}))
//            .doOnNext(new Action1<Integer>() {
//                @Override
//                public void call(Integer integer) {
//                    System.out.println("First: " + integer);
//                }
//            })
//            .filter(new Func1<Integer, Boolean>() {
//                @Override
//                public Boolean call(Integer integer) {
//                    return integer % 2 != 0;
//                }
//            })
//            .doOnNext(new Action1<Integer>() {
//                @Override
//                public void call(Integer integer) {
//                    System.out.println("After filter: " + integer);
//                    if (integer > 10) {
//                        Exceptions.propagate(new Exception("Bigger than 10"));
//                    }
//                }
//            })
//            .count()
//            .doOnNext(new Action1<Integer>() {
//                @Override
//                public void call(Integer integer) {
//                    System.out.println("After count: " + integer);
//                }
//            })
//            .map(new Func1<Integer, String>() {
//                @Override
//                public String call(Integer integer) {
//                    return String.format("Contains %d elements", integer);
//                }
//            });
//
//        Subscription subscription = someObservable.subscribe(
//            new Action1<String>() {
//                @Override
//                public void call(String s) {
//                    System.out.println("On subscription" + s);
//                }
//            }, new Action1<Throwable>() {
//                @Override
//                public void call(Throwable throwable) {
//                    System.out.println(throwable);
//                }
//            }, new Action0() {
//                @Override
//                public void call() {
//                    System.out.println("Completed!");
//                }
//            });
//    }




}
