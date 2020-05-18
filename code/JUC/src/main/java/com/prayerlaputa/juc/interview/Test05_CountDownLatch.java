package com.prayerlaputa.juc.interview;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/16
 */
public class Test05_CountDownLatch {


    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());



    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test05_CountDownLatch test = new Test05_CountDownLatch();

        CountDownLatch latch = new CountDownLatch(1);

        final Object lock = new Object();

        new Thread(() -> {
            if (test.size() != 5) {
                try {
                    latch.await();
                    System.out.println("t2 执行");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("t2 结束");
        }, "t2").start();


        new Thread(
                () -> {
                    for (int i = 0; i < 10; i++) {
                        test.add(i);
                        System.out.println("add " + i);

                        if (test.size() == 5) {
                            latch.countDown();
                        }

                        /*
                        若不休眠，只是countDown()一下，t1将继续占用CPU时间，t2无法获得CPU时间，无法执行。
                        最终执行结果就是t2虽然已经拿到了latch的锁，但只有t1稍微让出点时间，t2才能打印输出“t2执行”，
                        这个执行时间不一定，可能是t1执行完才继续执行t2，也可能是t1指定到第8个，...。所以这种做法还是没有
                        达到预期，因为没有精确在第5个时准备执行。
                         */
//                        try {
//                            TimeUnit.SECONDS.sleep(1);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }

                }
                , "t1").start();


    }
}
