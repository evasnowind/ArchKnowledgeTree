package com.prayerlaputa.juc.part2_sync.atomic;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author chenglong.yu
 * created on 2020/5/5
 */
public class TestAtomicVsLongVsAddr {
    static AtomicLong count1 = new AtomicLong(0L);
    static long count2 = 0L;
    static LongAdder count3 = new LongAdder();

    public static void main(String[] args) throws Exception{
        Thread[] threads = new Thread[1000];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int k = 0; k < 100000; k++) {
                    count1.incrementAndGet();
                }
            });
        }

        long start = System.currentTimeMillis();
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        long end = System.currentTimeMillis();

        System.out.println("Atomic " + count1.get() + "time:" + (end - start));

        //------------------------------------------
        Object obj = new Object();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int k = 0; k < 100000; k++) {
                                synchronized (obj) {
                                    count2++;
                                }
                            }
                        }
                    }
            );
        }

        start = System.currentTimeMillis();
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        end = System.currentTimeMillis();
        System.out.println("sync " + count1.get() + "time:" + (end - start));

        //------------------------------------------
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int k = 0; k < 100000; k++) {
                    count3.increment();
                }
            });
        }

        start = System.currentTimeMillis();
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        end = System.currentTimeMillis();
        System.out.println("LongAddr " + count3.longValue() + "time:" + (end - start));
    }

}
