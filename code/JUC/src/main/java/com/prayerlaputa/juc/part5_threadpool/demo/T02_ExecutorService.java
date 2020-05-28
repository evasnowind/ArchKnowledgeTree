/**
 * 认识ExecutorService,阅读API文档
 * 认识submit方法，扩展了execute方法，具有一个返回值
 */
package com.prayerlaputa.juc.part5_threadpool.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class T02_ExecutorService  {
    public static void main(String[] args) {
        ExecutorService e = Executors.newCachedThreadPool();
    }
}
