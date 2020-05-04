package com.prayerlaputa.juc.sync.t001;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/4
 */
public class TestSynchronized {

    public static void main(String[] args) {
        T t = new T();
        for (int i = 0; i < 100; i++) {
            new Thread(t, "THREAD" + i).start();
        }
    }
}
