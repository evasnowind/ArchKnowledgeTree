package com.prayerlaputa.juc.sync.t001;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/4
 */
public class T implements Runnable {
    private /*volatile*/ int count = 100;

    @Override
    public synchronized void run() {
        count--;
        System.out.println(Thread.currentThread().getName() + " count=" + count);
    }
}
