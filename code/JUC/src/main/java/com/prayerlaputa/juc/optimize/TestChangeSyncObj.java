package com.prayerlaputa.juc.optimize;


import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/5/4
 */
public class TestChangeSyncObj {

    final Object o = new Object();

    void m() {
        synchronized (o) {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName());
            }
        }
    }

    public static void main(String[] args) {
        TestChangeSyncObj t = new TestChangeSyncObj();
        new Thread(t::m, "t1").start();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread t2 = new Thread(t::m, "t2");
//        t.o = new Object();
        t2.start();
    }
}
