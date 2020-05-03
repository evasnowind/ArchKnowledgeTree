package com.prayerlaputa.juc.part2_sync.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author chenglong.yu
 * created on 2020/5/4
 */
public class HowToCreateThread {
    static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("Hello MyThread!");
        }
    }

    static class MyRun implements Runnable {
        @Override
        public void run() {
            System.out.println("Hello MyRun!");
        }
    }

    static class MyCall implements Callable<String> {
        @Override
        public String call() {
            System.out.println("Hello MyCall");
            return "success";
        }
    }

    //启动线程的5种方式
    public static void main(String[] args) {
        new MyThread().start();
        new Thread(new MyRun()).start();
        new Thread(() -> {
            System.out.println("Hello Lambda!");
        }).start();

        Thread t = new Thread(new FutureTask<String>(new MyCall()));
        t.start();

        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(() -> {
            System.out.println("Hello ThreadPool");
        });
        service.shutdown();
    }

}
