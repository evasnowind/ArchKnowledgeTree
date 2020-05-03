package com.prayerlaputa.juc.part2_sync.reentrantlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu
 * created on 2020/5/21
 */
public class T01_TestReentrantLock01 {


    Lock lock = new ReentrantLock();

    void m1() {
        try {
            lock.lock(); //synchronized(this)
            for (int i = 0; i < 10; i++) {
                TimeUnit.SECONDS.sleep(1);

                System.out.println(i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    void m2() {
        try {
            lock.lock();
            System.out.println("m2 ...");
        } finally {
            lock.unlock();
        }

    }

    public static void main(String[] args) {
        T01_TestReentrantLock01 rl = new T01_TestReentrantLock01();
        new Thread(rl::m1).start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(rl::m2).start();
    }
}
