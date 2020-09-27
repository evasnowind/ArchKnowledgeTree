package com.prayerlaputa.juc.interview.printA1B2C3;

/**
 * @author chenglong.yu
 * created on 2020/5/28
 */
public class T07_sync_wait_notify_ver3 {



    private static volatile boolean charThreadStart = false;

    public static void main(String[] args) {
        final Object object = new Object();

        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /**
         * wait/notify ��Condition.await/signalҲ��һ���ģ� �޷�������LockSupport.park/unpark��������ͨ������
         * wait/notify��λ�á�����������߼����ܱ�֤�����߳�����˳���ܰ�Ҫ���ӡ��
         * ��Դ���ڣ�LockSupport.park/unpark������park/unpark�������Ⱥ�˳����unpark��Ȼ����park��������park��unpark�����ԡ�
         * wait/notify ��Condition.await/signalҲ��һ���ģ��������wait(await)��notify(signal)
         *
         */
        Thread t1 = new Thread(() -> {
            synchronized (object) {
                for (char c : abcArr) {
                    try {
                        System.out.print(c);
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
