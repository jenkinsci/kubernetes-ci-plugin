package com.elasticbox.jenkins.k8s.repositories.api.charts;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import rx.Observable;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by serna on 4/18/16.
 */
public class TestYamlParse {




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
