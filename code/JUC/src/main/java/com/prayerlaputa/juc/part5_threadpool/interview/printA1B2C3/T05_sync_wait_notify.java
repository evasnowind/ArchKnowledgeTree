package com.prayerlaputa.juc.part5_threadpool.interview.printA1B2C3;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/27
 */
public class T05_sync_wait_notify {


    private static Object object = new Object();

    public static void main(String[] args) {
        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /**
         * 采用wait/notify的方法，所依赖的是线程启动顺序。
         * 线程内部输出语句倒是不用太留意。
         * 哪个线程先启动，立马输出，然后通知另个线程可以运行了，然后将自己锁住；
         * 另一个线程也是同样的逻辑：立马输出，然后通知，然后将自己锁住。
         *
         * 需要留意的是，由于是两个线程唤醒对方、然后自己睡眠，那么在最后有一个字符输出后，
         * 必然有一个线程会处于wait状态，为此在任何一个线程的for循环结束后，额外加一个notify操作，
         * 保证任何一个线程都不会卡死。
         */
        Thread t1 = new Thread(() -> {
            synchronized (object) {
                for (char c : abcArr) {
                    System.out.print(c);
                    try {
                        //注意此处不能sleep，sleep不会释放锁！
                        object.notify();
                        object.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                object.notify();
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            synchronized (object) {
                for (char c : numArr) {
                    try {
                        System.out.print(c);
                        object.notify();
                        object.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                object.notify();
            }
        }, "t2");

        t1.start();
        t2.start();
    }
}
