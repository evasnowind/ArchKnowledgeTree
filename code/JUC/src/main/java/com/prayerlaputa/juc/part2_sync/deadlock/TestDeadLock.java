package com.prayerlaputa.juc.part2_sync.deadlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu
 * created on 2020/7/12
 */
public class TestDeadLock {


    public static void main(String[] args) {

        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();


        new Thread(() -> {

            lock1.lock();
            try {
                System.out.println("t1 get lock1...");
                TimeUnit.SECONDS.sleep(10);
                lock2.lock();
                System.out.println("t1 get lock2...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock1.unlock();
                lock2.unlock();
            }
        }, "t1").start();

        new Thread(() -> {

            lock2.lock();
            try {
                System.out.println("t2 get lock2 ...");
                TimeUnit.SECONDS.sleep(10);
                lock1.lock();
                System.out.println("t2 get lock1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock2.unlock();
                lock1.unlock();
            }
        }, "t2").start();
    }
}
