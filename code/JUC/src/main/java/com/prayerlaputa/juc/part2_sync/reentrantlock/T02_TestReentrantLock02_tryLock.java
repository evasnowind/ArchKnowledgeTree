package com.prayerlaputa.juc.part2_sync.reentrantlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu
 * created on 2020/5/21
 */
public class T02_TestReentrantLock02_tryLock {


    Lock lock = new ReentrantLock();

    void m1() {
        try {
            lock.lock(); //synchronized(this)
            for (int i = 0; i < 3; i++) {
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


        boolean lockedFlag = false;
        try {
            lockedFlag = lock.tryLock(5, TimeUnit.SECONDS);
            if (lockedFlag) {
                System.out.println("m2 locked...");
            } else {
                System.out.println("m2 unlocked.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (lockedFlag) {
                /*
                此处如果没有上锁刘调用unlock方法，将会导致
                Exception in thread "Thread-1" java.lang.IllegalMonitorStateException
	at java.util.concurrent.locks.ReentrantLock$Sync.tryRelease(ReentrantLock.java:151)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.release(AbstractQueuedSynchronizer.java:1261)
	at java.util.concurrent.locks.ReentrantLock.unlock(ReentrantLock.java:457)
	at com.prayerlaputa.juc.sync.reentrantlock.TestReentrantLock02_tryLock.m2(TestReentrantLock02_tryLock.java:45)
	at java.lang.Thread.run(Thread.java:748)
                 */
                lock.unlock();
            }

        }
    }

    public static void main(String[] args) {
        T02_TestReentrantLock02_tryLock rl = new T02_TestReentrantLock02_tryLock();
        new Thread(rl::m1).start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(rl::m2).start();
    }
}
