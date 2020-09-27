package com.prayerlaputa.juc.interview.printA1B2C3;

/**
 * @author chenglong.yu
 * created on 2020/5/28
 */
public class T06_sync_wait_notify_ver2 {



    private static volatile boolean charThreadStart = false;

    public static void main(String[] args) {
        final Object object = new Object();

        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /**
         * �ڵ�һ��wait/notifyʵ���У��������̵߳�����˳��
         * �������ַ�ʽ�Դ������Ż���ͨ����������һ��volatile������
         * �����߳�ִ��˳�򣬲����ĸ��߳��������ȿ�ʼ��ӡ�Ķ��Ǵ�ӡ��ĸ���̡߳�
         */
        Thread t1 = new Thread(() -> {
            synchronized (object) {
                for (char c : abcArr) {
                    System.out.print(c);
                    charThreadStart = true;
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
