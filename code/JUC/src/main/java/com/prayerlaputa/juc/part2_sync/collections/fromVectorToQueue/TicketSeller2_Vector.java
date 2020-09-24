/**
 * 有N张火车票，每张票都有一个编号
 * 同时有10个窗口对外售票
 * 请写一个模拟程序
 * <p>
 * 分析下面的程序可能会产生哪些问题？
 * <p>
 * 使用Vector或者Collections.synchronizedXXX
 * 分析一下，这样能解决问题吗？
 *
 * @author 马士兵
 */
package com.prayerlaputa.juc.part2_sync.collections.fromVectorToQueue;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class TicketSeller2_Vector {
    static Vector<String> tickets = new Vector<>();


    static {
        for (int i = 0; i < 1000; i++) {
            tickets.add("票 编号：" + i);
        }
    }

    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                while (tickets.size() > 0) {
					/*
					仍会报异常：
					Exception in thread "Thread-6" Exception in thread "Thread-5" Exception in thread "Thread-2" Exception in thread "Thread-8" Exception in thread "Thread-0" Exception in thread "Thread-7" Exception in thread "Thread-4" Exception in thread "Thread-1" Exception in thread "Thread-9" java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 0
	at java.util.Vector.remove(Vector.java:831)
	at com.prayerlaputa.juc.part2_sync.collections.fromVectorToQueue.TicketSeller2.lambda$main$0(TicketSeller2.java:39)
	at java.lang.Thread.run(Thread.java:748)
java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 0
	at java.util.Vector.remove(Vector.java:831)
	at com.prayerlaputa.juc.part2_sync.collections.fromVectorToQueue.TicketSeller2.lambda$main$0(TicketSeller2.java:39)
	at java.lang.Thread.run(Thread.java:748)

					原因：虽然tickets用了Vector，保证容器本身是线程安全的，但是在
					while(tickets.size() > 0)  与 tickets.remove(0) 这两个操作，没有保证原子性。
					可能A、B两个线程调用tickets.size()时，tickets刚好只剩下1个元素，于是。。。
					 */
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    System.out.println("销售了--" + tickets.remove(0));
                }
            }).start();
        }
    }
}
