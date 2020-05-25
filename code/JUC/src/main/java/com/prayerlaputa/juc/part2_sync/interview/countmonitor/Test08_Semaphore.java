package com.prayerlaputa.juc.part2_sync.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/16
 */
public class Test08_Semaphore {


    volatile List list = new LinkedList();

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    static Thread t1 = null, t2 = null;

    public static void main(String[] args) {
        Test08_Semaphore test = new Test08_Semaphore();

        Semaphore s = new Semaphore(1);

        t1 = new Thread(() -> {
            try {
                s.acquire();
                for (int i = 0; i < 5; i++) {
                    test.add(new Object());
                    System.out.println("add " + i);


                }
                s.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                /*
                这种利用信号量的解法，实际上跟题目要求有些相悖，因为这里只是判断=5，那如果换个条件
                循环就不好说了。此处只是为了展示下信号量写法。
                为了保证t1先执行，t2在t1执行后才开始start
                 */
                t2.start();
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                s.acquire();
                for (int i = 5; i < 10; i++) {
                    System.out.println(i);
                }
                s.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, "t1");

        t2 = new Thread(() -> {
            try {
                s.acquire();
                System.out.println("t2 结束");
                s.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t2");

        //t2.start();
        t1.start();
    }
}
