package com.prayerlaputa.juc.part2_sync.reentrantlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu
 * created on 2020/5/21
 */
public class T03_TestReentrantLock03_LockInterruptibly {


    Lock lock = new ReentrantLock();

    void m1() {
        try {
            lock.lock(); //synchronized(this)
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
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
            lock.lockInterruptibly();
            System.out.println("m2 locked...");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    static Thread t2;

    void m3() {
        try {
            TimeUnit.SECONDS.sleep(5);
            //中断t2线程，以便观察t2是什么反应
            if (null != t2) {
                System.out.println("m3 调用t2.interrupt方法");
                t2.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        T03_TestReentrantLock03_LockInterruptibly rl = new T03_TestReentrantLock03_LockInterruptibly();
        new Thread(rl::m1).start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t2 = new Thread(rl::m2);
        t2.start();

        new Thread(rl::m3).start();
    }
}
