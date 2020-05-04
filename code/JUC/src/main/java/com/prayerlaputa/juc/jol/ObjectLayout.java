package com.prayerlaputa.juc.jol;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/2
 */
public class ObjectLayout {

    public static void main(String[] args) throws Exception {
        Thread.sleep(5000);

        Object o = new Object();



        System.out.println(ClassLayout.parseInstance(o).toPrintable());


        synchronized (o) {
            System.out.println(ClassLayout.parseInstance(o).toPrintable());
        }
    }
}
