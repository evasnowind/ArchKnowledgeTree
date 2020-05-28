package com.prayerlaputa.juc.part5_threadpool.interview.printA1B2C3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/28
 */
public class T09_lock_condition_ver2 {

    private static volatile boolean charThreadStart = false;

    public static void main(String[] args) {


        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        ReentrantLock lock = new ReentrantLock();
        Condition charCondition = lock.newCondition();
        Condition numCondition = lock.newCondition();

        Thread t1 = new Thread(() -> {
            try {
                lock.lock();

                for (char c : abcArr) {
                    System.out.print(c);
                    //注意此处不能sleep，sleep不会释放锁！
                    numCondition.signal();
                    charCondition.await();
                }
                numCondition.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            try {
                lock.lock();

                for (char c : numArr) {
                    numCondition.await();
                    System.out.print(c);
                    charCondition.signal();
                }
                charCondition.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t2");

        /**
         * 注意，由于Condition的await/signal操作，并不像LockSupport.park/unpark那样不用考虑调用先后，
         * 必须是await在先、然后再调用signal。这个限制导致如果想用Condition锁住线程、保证顺序时，线程启动
         * 要保证一定顺序。比如说，如果上面的写法，t1先执行，将会一直死锁，因为t1在numCondition.await时，t2的
         * numCondition.signal已执行。
         * 先执行t2，则将会是t2先执行、被numCondition.await()锁住，然后t1执行、输出字母、numCondition.signal使得t2可以执行，
         * 然后t1被charCondition.await锁住。如此往复。
         */
//        t1.start();
        t2.start();
        t1.start();
    }
}
