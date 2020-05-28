package com.prayerlaputa.juc.part5_threadpool.interview.printA1B2C3;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/27
 */
public class T01_LockSupport {

    static Thread t1 = null, t2 =null;

    public static void main(String[] args) {

        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /*
         * 为了控制打印顺序，两个线程分别给对方上锁、解锁。
         * 此处有个细节，print的位置决定了输出字符串中数字、字母的先后顺序
         *
         */
        t1 = new Thread(() -> {
            for (int i = 0; i < numArr.length; i++) {
                //park方法锁住的是当前线程，如果调用park(obj)，也不过是利用obj作为blocker，参见LockSupport源码。
                LockSupport.park();
                System.out.print(numArr[i]);
                //unpark方法必须传入想要解锁的线程
                LockSupport.unpark(t2);
            }
        }, "t1");
        t2 = new Thread(() -> {
            for (int i = 0; i < abcArr.length; i++) {
                //先打印再unpark，或是先unpark再打印，都可以保证先输出字母再输出数组
//                System.out.print(abcArr[i]);
                LockSupport.unpark(t1);
                System.out.print(abcArr[i]);
                //主要是下面这一句控制
                LockSupport.park();
                //如果在这里，则输出先数字、然后字母的字符串
//                System.out.print(abcArr[i]);
            }
        });

        //t1/t2谁先启动并无影响，因为主要是靠park来控制输出顺序
        t2.start();
        t1.start();
    }
}
