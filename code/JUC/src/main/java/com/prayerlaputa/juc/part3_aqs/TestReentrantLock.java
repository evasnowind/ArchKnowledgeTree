package com.prayerlaputa.juc.part3_aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TestReentrantLock {

    private static volatile int i = 0;

    private static ReentrantLock lock = new ReentrantLock(true);

    public static void main(String[] args) {

        //
        /*
        1. 直接启动下面程序，不会lock()方法内部将不会调用ReentrantLock的acquire方法
        为了走acquire，稍微排除下可能性，是否可能是偏向锁引起的？因为JVM启动时默认启动偏向锁。
        先禁用偏向锁，用下面参数，或是延迟5s（参见偏向锁相关帖子）
        -XX:-UseBiasedLocking -client -Xmx512m -Xms512m
        结果仍不会走acquire方法，基本可以确定，是因为没有竞争、所以没走acquire方法。

        2. 接下来，通过额外添加一个线程、造成竞争同一把锁的情况，此时在主线程中，lock.lock()方法
        内部将会走acquire方法
         */

//        try {
//            TimeUnit.SECONDS.sleep(5);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        new Thread(()-> {
            lock.lock();
            i = i + 2;
            System.out.println("sub thread:i=" + i);
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock();
        }).start();


        lock.lock();
        //synchronized (TestReentrantLock.class) {
            i++;
        //}

        lock.unlock();
    }


}
