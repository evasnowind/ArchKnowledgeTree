package com.prayerlaputa.juc.part6_others.guava_ratelimiter;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/9/2
 */
public class TestGuavaRateLimiter {


    public static void main(String[] args) {

        RateLimiter rateLimiter = RateLimiter.create(1);
        int n = 5;
        final int sleepSec = 3;
        CyclicBarrier barrier = new CyclicBarrier(n);
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                try {
                    barrier.await();
                    rateLimiter.acquire();
                    System.out.println(new Date() + Thread.currentThread().getName() + " get token");
                    TimeUnit.SECONDS.sleep(sleepSec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        try {
            TimeUnit.SECONDS.sleep(sleepSec * n);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
