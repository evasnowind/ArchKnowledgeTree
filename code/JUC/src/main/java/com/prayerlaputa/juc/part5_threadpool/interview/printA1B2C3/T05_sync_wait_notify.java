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
         * 采用wait/notify的方法，
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
