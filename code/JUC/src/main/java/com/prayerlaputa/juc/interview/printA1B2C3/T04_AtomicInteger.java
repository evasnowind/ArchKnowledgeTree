package com.prayerlaputa.juc.interview.printA1B2C3;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenglong.yu
 * created on 2020/5/27
 */
public class T04_AtomicInteger {

    /**
     * ע��˴����ü�volatile
     * AtomicInteger�Լ��Ѿ����̰߳�ȫ
     */
    private static AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) {
        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        new Thread(() -> {
            for (int i = 0; i < abcArr.length; i++) {
                while(1 != counter.get()) {}
                System.out.print(abcArr[i]);
                counter.set(0);
            }
        }, "t1").start();
        new Thread(() -> {
            for (int i = 0; i < numArr.length; i++) {
                while (0 != counter.get()) {}
                System.out.print(numArr[i]);
                counter.set(1);
            }
        }, "t2").start();

    }
}
