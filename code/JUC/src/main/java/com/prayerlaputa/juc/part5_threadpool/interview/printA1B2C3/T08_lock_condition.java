package com.prayerlaputa.juc.part5_threadpool.interview.printA1B2C3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/28
 */
public class T08_lock_condition {

    private static volatile boolean charThreadStart = false;

    public static void main(String[] args) {


        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        /**
         * 本代码的实现，实际上与T06_sync_wait_notify_ver2是一样的，只不过使用的锁
         * 从synchronized/wait/notify换成了ReentrantLock/Condition。
         * 注意Condition使用时需要用signal/await方法，而不是wait/notify
         */
        Thread t1 = new Thread(() -> {
            try {
                lock.lock();

                for (char c : abcArr) {
                    System.out.print(c);
                    charThreadStart = true;
                    //注意此处不能sleep，sleep不会释放锁！
                    condition.signal();
                    condition.await();
                }
                condition.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            try {
                lock.lock();

                while (!charThreadStart) {
                    condition.await();
                }

                for (char c : numArr) {
                    charThreadStart = false;
                    System.out.print(c);
                    condition.signal();
                    condition.await();
                }
                condition.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t2");

//        t1.start();
        t2.start();
        t1.start();
    }
}
