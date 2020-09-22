package com.prayerlaputa.juc.part5_threadpool.demo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/9/21
 */
public class T15_01_ExceptionInThreadPool {


    public static void main(String[] args) throws InterruptedException {

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

        System.out.println("=================================================");
        try {
            executor.execute(() -> sayHi("execute task"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        主线程睡一会，以便让上面出异常的线程输出异常信息。
        若不睡一会，此处将只能看到下面这个线程输出的异常信息。
         */
        TimeUnit.MILLISECONDS.sleep(50);

        System.out.println("=================================================");
        try {
            Future future = executor.submit(() -> sayHi("submit task"));
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void sayHi(String name) {
        String str = "【thread-name:" + Thread.currentThread().getName() + ",执行方式:" + name+"】";
        System.out.println(str);
        throw new RuntimeException(str + ", 当前程序异常！");
    }
}
