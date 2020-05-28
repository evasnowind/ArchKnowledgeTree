package com.prayerlaputa.juc.part5_threadpool.interview.printA1B2C3;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/27
 */
public class T03_BlockingQueue {

    private static BlockingQueue blockingQueue1 = new ArrayBlockingQueue(1);
    private static BlockingQueue blockingQueue2 = new ArrayBlockingQueue(1);

    public static void main(String[] args) {
        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /**
         * 实际上这里用阻塞队列和用LockSupport.park unpark原理是一样的
         * 注意不要两边上来都take，否则将导致两个线程都被阻塞
         *
         * 另外，一定要记住，对于实现了BlockingQueue的类，offer/poll 是非阻塞的，
         * put/take是阻塞的。
         *
         */
        new Thread(() -> {
            for (int i = 0; i < abcArr.length; i++) {
                try {
                    System.out.print(abcArr[i]);
                    blockingQueue2.put("hello t2");
                    Object object = blockingQueue1.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t1").start();
        new Thread(() -> {
            for (int j = 0; j < numArr.length; j++) {
                try {
                    Object object = blockingQueue2.take();
                    System.out.print(numArr[j]);
                    blockingQueue1.put("hello t1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t2").start();
    }
}
