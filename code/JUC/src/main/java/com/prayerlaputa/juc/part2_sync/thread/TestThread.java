package com.prayerlaputa.juc.part2_sync.thread;

import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/5/1
 */
public class TestThread {

    private static class T1 extends Thread {
        @Override
        public void run() {
            for(int i=0; i<10; i++) {
                try {
                    TimeUnit.MICROSECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("T1");
            }
        }
    }

    public static void main(String[] args) {
//        new T1().run();
        new T1().start();
        for(int i=0; i<10; i++) {
            try {
                TimeUnit.MICROSECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("main");
        }

    }
}
