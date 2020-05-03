package com.prayerlaputa.juc.part1_jol;

import org.openjdk.jol.info.ClassLayout;

import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/6/20
 */
public class IntegerObjectLayout {

    public static void main(String[] args) {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Integer intObj = new Integer(13);
        System.out.println(ClassLayout.parseInstance(intObj).toPrintable());
    }
}
