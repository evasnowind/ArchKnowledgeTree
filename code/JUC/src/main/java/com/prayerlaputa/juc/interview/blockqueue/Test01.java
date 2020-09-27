package com.prayerlaputa.juc.interview.blockqueue;

import java.util.LinkedList;

/**
 * @author chenglong.yu
 * created on 2020/5/18
 */
public class Test01<T> {

    private LinkedList<T> lists = new LinkedList<>();

    private int MAX_CAPACITY = 10;

    /**
     * 使用volatile修饰，保证多线程可见性
     */
    private volatile int count = 0;

    public synchronized void put(T t) {
        while (lists.size() == MAX_CAPACITY) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        lists.add(t);
        count += 1;

        this.notifyAll();
    }

    public int getCount() {
        return count;
    }

    public synchronized T get() {
        T t = null;
        while (lists.size() == 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        count -= 1;
        t = lists.removeFirst();
        this.notifyAll();

        return t;
    }

    public static void main(String[] args) {
        Test01 test01 = new Test01();
        for (int i = 0; i < 10; i++) {
            final int tmp = i;
            Thread t = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    test01.put(j);
                    System.out.println("存入" + tmp + " " + j + " count=" + test01.getCount());
                }
            }
            );
            t.start();
        }

//        try {
//            TimeUnit.SECONDS.sleep(2);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        for (int i = 0; i < 2; i++) {
            new Thread(() ->{
                for(int j = 0; j < 50; j++) {
                    System.out.println("获取" + test01.get() + " count=" + test01.getCount());
                }

            }).start();
        }
    }
}
