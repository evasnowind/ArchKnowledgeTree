package com.prayerlaputa.juc.interview.printA1B2C3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu
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
         * �������ʵ�֣�ʵ������T06_sync_wait_notify_ver2��һ���ģ�ֻ����ʹ�õ���
         * ��synchronized/wait/notify������ReentrantLock/Condition��
         * ע��Conditionʹ��ʱ��Ҫ��signal/await������������wait/notify
         */
        Thread t1 = new Thread(() -> {
            try {
                lock.lock();

                for (char c : abcArr) {
                    System.out.print(c);
                    charThreadStart = true;
                    //ע��˴�����sleep��sleep�����ͷ�����
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
