package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/15
 */
public class Test04_NotifyHoldingLock {


    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test04_NotifyHoldingLock test = new Test04_NotifyHoldingLock();

        final Object lock = new Object();

        new Thread(() -> {
            synchronized (lock) {
                System.out.println("t2 开始");
                if (test.size() != 5) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("t2 结束");
            }
        }, "t2").start();


        new Thread(
                () -> {
                    System.out.println("t1启动");
                    synchronized (lock) {
                        for (int i = 0; i < 10; i++) {
                            test.add(i);
                            System.out.println("add " + i);

                        /*
                        调用wait/notify 之前必须加上synchronized，否则将报如下异常
                        Exception in thread "t1" java.lang.IllegalMonitorStateException
	at java.lang.Object.notify(Native Method)
	at com.prayerlaputa.juc.interview.countmonitor.Test03_WaitNotify.lambda$main$1(Test03_WaitNotify.java:51)
	at java.lang.Thread.run(Thread.java:748)

                         */
                            if (test.size() == 5) {
                                //由于notify并没有释放lock锁，当前线程仍在运行，仍拿着lock锁，因此无法切换到线程2，无法到达目的
                                lock.notify();
                            }

                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    System.out.println("t1结束");
                }
                , "t1").start();
    }
}
