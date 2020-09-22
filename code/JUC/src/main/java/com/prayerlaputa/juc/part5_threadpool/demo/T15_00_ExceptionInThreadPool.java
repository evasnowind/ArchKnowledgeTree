package com.prayerlaputa.juc.part5_threadpool.demo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/9/21
 */
public class T15_00_ExceptionInThreadPool {


    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Execute hook ...");
            }
        }));


        ThreadPoolExecutor executor = new ThreadPoolExecutor(2,
                10,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "ThreadPool_" + r.hashCode());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy());


        executor.submit(() -> {
            while(true) {
                TimeUnit.MILLISECONDS.sleep(500);
                System.out.println("thread pool: active thread count=" + executor.getActiveCount() + " current thread cnt=" + executor.getPoolSize());
            }
        });

        try {
            executor.execute(() -> sayHi("execute task"));
        } catch (Exception e) {
            System.out.println("execute task throw exception, info=" + e);
        }

        try {
            executor.submit(() -> sayHi("submit task"));
        } catch (Exception e) {
            System.out.println("submit task throw exception, info=" + e);
        }

    }

    private static void sayHi(String name) {
        String str = "【thread-name:" + Thread.currentThread().getName() + ",执行方式:" + name+"】";
        System.out.println(str);
        throw new RuntimeException(str + ", 当前程序异常！");
    }
}
