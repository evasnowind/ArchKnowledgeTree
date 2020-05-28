package com.prayerlaputa.juc.part5_threadpool.interview.printA1B2C3;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/28
 */
public class T06_sync_wait_notify_ver2 {



    private static volatile boolean charThreadStart = false;

    public static void main(String[] args) {
        final Object object = new Object();

        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /**
         * 在第一版wait/notify实现中，依赖于线程的启动顺序。
         * 下面这种方式对此做了优化，通过引入额外的一个volatile变量，
         * 控制线程执行顺序，不管哪个线程启动，先开始打印的都是打印字母的线程。
         */
        Thread t1 = new Thread(() -> {
            synchronized (object) {
                for (char c : abcArr) {
                    System.out.print(c);
                    charThreadStart = true;
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
                while (!charThreadStart) {
                    try {
                        object.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (char c : numArr) {
                    charThreadStart = false;
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
//        t1.start();
    }
}
