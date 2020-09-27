package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/5/15
 */
public class Test04_NotifyHoldingLock {


    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test04_NotifyHoldingLock test = new Test04_NotifyHoldingLock();

        final Object lock = new Object();

        new Thread(() -> {
            synchronized (lock) {
                System.out.println("t2 ��ʼ");
                if (test.size() != 5) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("t2 ����");
            }
        }, "t2").start();


        new Thread(
                () -> {
                    System.out.println("t1����");
                    synchronized (lock) {
                        for (int i = 0; i < 10; i++) {
                            test.add(i);
                            System.out.println("add " + i);

                            if (test.size() == 5) {
                                //����notify��û���ͷ�lock������ǰ�߳��������У�������lock��������޷��л����߳�2���޷�����Ŀ��
                                lock.notify();
                            }

                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    System.out.println("t1����");
                }
                , "t1").start();
    }
}
