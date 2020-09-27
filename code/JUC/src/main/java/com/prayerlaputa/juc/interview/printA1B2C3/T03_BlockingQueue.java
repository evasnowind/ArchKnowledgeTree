package com.prayerlaputa.juc.interview.printA1B2C3;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author chenglong.yu
 * created on 2020/5/27
 */
public class T03_BlockingQueue {

    private static BlockingQueue blockingQueue1 = new ArrayBlockingQueue(1);
    private static BlockingQueue blockingQueue2 = new ArrayBlockingQueue(1);

    public static void main(String[] args) {
        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /**
         * ʵ�����������������к���LockSupport.park unparkԭ����һ����
         * ע�ⲻҪ����������take�����򽫵��������̶߳�������
         *
         * ���⣬һ��Ҫ��ס������ʵ����BlockingQueue���࣬offer/poll �Ƿ������ģ�
         * put/take�������ġ�
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
