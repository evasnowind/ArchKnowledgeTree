package com.prayerlaputa.juc.sync;

import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/4
 */
public class T05_TestSynchronizedExtend {

    synchronized void m1() {
        System.out.println("m1 start");

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("m1 end");
    }

    public static void main(String[] args) {
        new Child().m1();
    }
}

class Child extends T05_TestSynchronizedExtend{
    @Override
    synchronized void m1() {
        System.out.println("child m start");
        super.m1();
        System.out.println("child m end");
    }
}
