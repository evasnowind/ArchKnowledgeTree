package com.prayerlaputa.juc.testvolatile;

/**
 * @author chenglong.yu
 * created on 2020/5/4
 */
public class TestThreadVisibility {
    private static volatile boolean flag = true;

    public static void main(String[] args) throws InterruptedException {
        new Thread(()-> {
            while (flag) {
                //do sth
            }
            System.out.println("end");
        }, "server").start();


        Thread.sleep(1000);

        flag = false;
    }
}
