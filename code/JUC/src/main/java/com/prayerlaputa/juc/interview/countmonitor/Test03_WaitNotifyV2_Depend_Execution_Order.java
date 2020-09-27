package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author chenglong.yu
 * created on 2020/6/9
 */
public class Test03_WaitNotifyV2_Depend_Execution_Order {
    List list = new LinkedList();

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test03_WaitNotifyV2_Depend_Execution_Order test = new Test03_WaitNotifyV2_Depend_Execution_Order();

        final Object lock = new Object();

        new Thread(
                () -> {
                    synchronized (lock) {
                        for (int i = 0; i < 10; i++) {
                            test.add(i);
                            System.out.println("add " + i);

                            if (i == 4) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }
                , "t1").start();

        new Thread(() -> {
            synchronized (lock) {
                System.out.println("t2 list size=5");
                lock.notifyAll();
            }
        }, "t2").start();
    }
}
