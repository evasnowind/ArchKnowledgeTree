package com.prayerlaputa.juc.interview.park_thread;

import java.util.concurrent.locks.LockSupport;

/**
 * @author chenglong.yu
 * created on 2020/9/24
 */
public class ParkDemo {
    public static void main(String[] args) {
        Thread threadA = new Thread(new TaskA());
        threadA.start();

        Thread threadB = new Thread(new TaskB(threadA));
        threadB.start();

        while (true) {

        }
    }

    private static class TaskA implements Runnable {

        @Override
        public void run() {
            for (; ; ) {
                System.out.println("ThreadA is running");
                System.out.println("ThreadA call park()------------");
                LockSupport.park();
                System.out.println("ThreadA is continues>>>");
            }
        }
    }

    private static class TaskB implements Runnable {
        private final Thread threadA;

        public TaskB(Thread thread) {
            threadA = thread;
        }

        @Override
        public void run() {
            for (int i=0; i< 10; i++) {
                System.out.println("ThreadB sleep for 5 seconds.");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("ThreadB now unpark ThreadA");
                LockSupport.unpark(threadA);
            }
        }
    }
}
