package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/16
 */
public class Test06_LockSupport {


    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());



    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test06_LockSupport test = new Test06_LockSupport();

        final Object lock = new Object();

        Thread t1 = null;
        Thread t2 = new Thread(() -> {
            if (test.size() != 5) {
                LockSupport.park();
                System.out.println("t2 执行");
            }
            System.out.println("t2 结束");
//            LockSupport.unpark(t1);
        }, "t2");
        t2.start();

        t1 = new Thread(
                () -> {
                    for (int i = 0; i < 10; i++) {
                        test.add(i);
                        System.out.println("add " + i);

                        if (test.size() == 5) {
                            LockSupport.unpark(t2);
                        }

                        /*
                        若不休眠，t1将继续占用CPU时间，t2无法获得CPU时间，无法执行。
                        最终执行结果就是t2虽然已经拿到了锁，但只有t1稍微让出点时间，t2才能打印输出“t2执行”，
                        这个执行时间不一定，可能是t1执行完才继续执行t2，也可能是t1指定到第8个，...。所以这种做法还是没有
                        达到预期，因为没有精确在第5个时准备执行。
                         */
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }
                , "t1");
        t1.start();


    }
}
