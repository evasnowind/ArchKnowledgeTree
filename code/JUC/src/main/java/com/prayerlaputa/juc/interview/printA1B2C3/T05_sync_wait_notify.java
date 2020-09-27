package com.prayerlaputa.juc.interview.printA1B2C3;

/**
 * @author chenglong.yu
 * created on 2020/5/27
 */
public class T05_sync_wait_notify {


    private static Object object = new Object();

    public static void main(String[] args) {
        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /**
         * ����wait/notify�ķ����������������߳�����˳��
         * �߳��ڲ������䵹�ǲ���̫���⡣
         * �ĸ��߳������������������Ȼ��֪ͨ����߳̿��������ˣ�Ȼ���Լ���ס��
         * ��һ���߳�Ҳ��ͬ�����߼������������Ȼ��֪ͨ��Ȼ���Լ���ס��
         *
         * ��Ҫ������ǣ������������̻߳��ѶԷ���Ȼ���Լ�˯�ߣ���ô�������һ���ַ������
         * ��Ȼ��һ���̻߳ᴦ��wait״̬��Ϊ�����κ�һ���̵߳�forѭ�������󣬶����һ��notify������
         * ��֤�κ�һ���̶߳����Ῠ����
         */
        Thread t1 = new Thread(() -> {
            synchronized (object) {
                for (char c : abcArr) {
                    System.out.print(c);
                    try {
                        //ע��˴�����sleep��sleep�����ͷ�����
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
