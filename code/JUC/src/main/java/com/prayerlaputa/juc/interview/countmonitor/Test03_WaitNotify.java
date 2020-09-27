package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author chenglong.yu
 * created on 2020/5/15
 */
public class Test03_WaitNotify {


    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test03_WaitNotify test = new Test03_WaitNotify();

        final Object lock = new Object();

        new Thread(() -> {
            synchronized (lock) {
                System.out.println("t2 ��ʼ");
                if (test.size() != 5) {
                    try {
                        lock.wait();
                        System.out.println("t2ִ�У�����size=5");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("t2 ����");

                /*
                ��ʱt1�̻߳���wait����Ҫ����
                 */
                lock.notify();
            }
        }, "t2").start();


        new Thread(
                () -> {
                    System.out.println("t1����");
                    synchronized (lock) {
                        for (int i = 0; i < 10; i++) {
                            test.add(i);
                            System.out.println("add " + i);

                        /*
                        ����wait/notify ֮ǰ�������synchronized�����򽫱������쳣
                        Exception in thread "t1" java.lang.IllegalMonitorStateException
	at java.lang.Object.notify(Native Method)
	at com.prayerlaputa.juc.interview.countmonitor.Test03_WaitNotify.lambda$main$1(Test03_WaitNotify.java:51)
	at java.lang.Thread.run(Thread.java:748)

                         */
                            if (test.size() == 5) {
                                //����notify��û���ͷ�lock������noti֮�󣬵�ǰ�߳��������У�������lock��������޷��л����߳�2
                                lock.notify();

                                try {
                                    /*
                                    �˴�ͨ��lock.wait������ǰ�̣߳��ͷ��������뵽���ȶ����У��ó�CPU��ʹ��t2�߳��л���ִ��
                                     */
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

//                            try {
//                                TimeUnit.SECONDS.sleep(1);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }

                    System.out.println("t1����");
                }
                , "t1").start();
    }
}
