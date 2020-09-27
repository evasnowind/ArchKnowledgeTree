package com.prayerlaputa.juc.interview.countmonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/5/15
 */
public class Test01_NoVolatile {

    List list = new ArrayList();

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test01_NoVolatile test = new Test01_NoVolatile();
        new Thread(
                () -> {
                    for (int i = 0; i < 10; i++) {
                        test.add(i);
                        System.out.println("add " + i);

                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
        , "t1").start();

        new Thread(() -> {
            //多线程，没加volatile，t2线程根本看不到t1中的变化
            while(true) {
                if (test.size() == 5) {
                    break;
                }
            }
            System.out.println("t2 结束");
        }, "t2").start();
    }
}
