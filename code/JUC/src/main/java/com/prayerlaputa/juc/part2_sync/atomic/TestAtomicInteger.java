package com.prayerlaputa.juc.part2_sync.atomic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenglong.yu
 * created on 2020/5/2
 */
public class TestAtomicInteger {

    static AtomicInteger count  = new AtomicInteger();

    public static void main(String[] args) {
        Thread[] threads = new Thread[100];;
        CountDownLatch latch = new CountDownLatch(threads.length);
        for (int i = 0; i < threads.length; i++) {
            Runnable target;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    count.getAndIncrement();
                }

                latch.countDown();
            });
            threads[i].start();
        }

        System.out.println(count.get());
    }
}
