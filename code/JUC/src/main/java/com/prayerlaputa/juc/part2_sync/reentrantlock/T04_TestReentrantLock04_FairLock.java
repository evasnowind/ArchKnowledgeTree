package com.prayerlaputa.juc.part2_sync.reentrantlock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu
 * created on 2020/5/22
 */
public class T04_TestReentrantLock04_FairLock {

    private static CountDownLatch latch = new CountDownLatch(200);
    private ReentrantLock nonFairLock = new ReentrantLock();
    private ReentrantLock fairLock = new ReentrantLock(true);

    public void printWithNonFairLock() {
        for (int i = 0; i < 100; i++) {
            try {
                nonFairLock.lock();
                System.out.println("" + Thread.currentThread().getName() + "获取锁");
                TimeUnit.MICROSECONDS.sleep(100);
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                nonFairLock.unlock();
            }
        }

    }

    public void printWithFairLock() {
        for (int i = 0; i < 100; i++) {
            try {
                fairLock.lock();
                System.out.println("" + Thread.currentThread().getName() + "获取锁");
                TimeUnit.MICROSECONDS.sleep(50);
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fairLock.unlock();
            }
        }

    }

    public static void main(String[] args) {
        T04_TestReentrantLock04_FairLock test = new T04_TestReentrantLock04_FairLock();

        new Thread(test::printWithNonFairLock, "t1").start();
        new Thread(test::printWithNonFairLock, "t2").start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("----------------------------countdown 结束！----------------------------");

        new Thread(test::printWithFairLock, "t3").start();
        new Thread(test::printWithFairLock, "t4").start();
    }

}
