package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

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

        final Object lock = new Object();

        t2 = new Thread(() -> {
            if (test.size() != 5) {
                LockSupport.park();
                System.out.println("t2 执行");
            }
            System.out.println("t2 结束");
            LockSupport.unpark(t1);
        }, "t2");
        t2.start();

        t1 = new Thread(
                () -> {
                    for (int i = 0; i < 10; i++) {
                        test.add(i);
                        System.out.println("add " + i);

                        if (test.size() == 5) {
                            LockSupport.unpark(t2);
                            //此处阻塞是为了暂时让出CPU，调度t2执行，所以t2中也相应的有unpark(t1)的代码，以便将控制权交回给t1
                            LockSupport.park();
                        }
                    }

                }
                , "t1");
        t1.start();


    }
}
