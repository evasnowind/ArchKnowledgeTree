package com.prayerlaputa.juc.part2_sync.interview.countmonitor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/15
 */
public class Test02_WithVolatile {

//    volatile List list = new LinkedList();

    volatile List list = Collections.synchronizedList(new LinkedList<>());

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test02_WithVolatile test = new Test02_WithVolatile();
        new Thread(
                () -> {
                    for (int i = 0; i < 10; i++) {
                        test.add(i);
                        System.out.println("add " + i);

                        //如果不sleep，test的变化就无法通知到t2线程。应该是sleep时有一些同步操作
                        /*
                        结论：
                        1、volatile 没有把握的话不要轻易用
                        2、volatile修饰的变量，尽量是基本类型，不要修饰类似List这种引用类型，因为List内部变化、对象本身
                        没有变化，volatile可能就观测不到
                         */
//                        try {
//                            TimeUnit.SECONDS.sleep(1);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }

                }
        , "t1").start();

        new Thread(() -> {
            while(true) {
                if (test.size() == 5) {
                    break;
                }
            }
            System.out.println("t2 结束");
        }, "t2").start();
    }
}
