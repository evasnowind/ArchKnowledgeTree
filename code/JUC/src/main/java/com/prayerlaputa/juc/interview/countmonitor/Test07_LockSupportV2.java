package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/16
 */
public class Test07_LockSupportV2 {


    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());



    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    static Thread t1 = null, t2 = null;

    public static void main(String[] args) {
        Test07_LockSupportV2 test = new Test07_LockSupportV2();

        Semaphore s = new Semaphore(1);

        t1 = new Thread(
                () -> {
                    try {
                        s.acquire();
                        for (int i = 0; i < 5; i++) {
                            test.add(i);
                            System.out.println("add " + i);
                        }
                        s.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    try {
                        t2.start();
                        t2.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        s.acquire();
                        for (int i = 5; i < 10; i++) {
                            test.add(i);
                            System.out.println("add " + i);
                        }
                        s.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                , "t1");


        t2 = new Thread(() -> {
            System.out.println("t2开始");
            if (test.size() != 5) {
                try {
                    s.acquire();
                    System.out.println("t2 执行");
                    s.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            System.out.println("t2 结束");

        }, "t2");

        t1.start();



    }
}
