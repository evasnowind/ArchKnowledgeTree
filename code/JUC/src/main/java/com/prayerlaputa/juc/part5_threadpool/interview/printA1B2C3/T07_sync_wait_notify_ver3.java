package com.prayerlaputa.juc.part5_threadpool.interview.printA1B2C3;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/28
 */
public class T07_sync_wait_notify_ver3 {



    private static volatile boolean charThreadStart = false;

    public static void main(String[] args) {
        final Object object = new Object();

        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /**
         * wait/notify （Condition.await/signal也是一样的） 无法做到像LockSupport.park/unpark那样，仅通过调整
         * wait/notify的位置、不引入额外逻辑就能保证任意线程启动顺序都能按要求打印。
         * 根源在于：LockSupport.park/unpark操作，park/unpark不讲求先后顺序，先unpark，然后再park，或是先park再unpark都可以。
         * wait/notify （Condition.await/signal也是一样的）则必须先wait(await)再notify(signal)
         *
         */
        Thread t1 = new Thread(() -> {
            synchronized (object) {
                for (char c : abcArr) {
                    try {
                        System.out.print(c);
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
                        object.wait();
                        System.out.print(c);
                        object.notify();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                object.notify();
            }
        }, "t2");

//        t1.start();
        t2.start();
        t1.start();
    }
}
