package com.prayerlaputa.juc.interview.printA1B2C3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu
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
                    //ע��˴�����sleep��sleep�����ͷ�����
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
         * ע�⣬����Condition��await/signal������������LockSupport.park/unpark�������ÿ��ǵ����Ⱥ�
         * ������await���ȡ�Ȼ���ٵ���signal��������Ƶ����������Condition��ס�̡߳���֤˳��ʱ���߳�����
         * Ҫ��֤һ��˳�򡣱���˵����������д����t1��ִ�У�����һֱ��������Ϊt1��numCondition.awaitʱ��t2��
         * numCondition.signal��ִ�С�
         * ��ִ��t2���򽫻���t2��ִ�С���numCondition.await()��ס��Ȼ��t1ִ�С������ĸ��numCondition.signalʹ��t2����ִ�У�
         * Ȼ��t1��charCondition.await��ס�����������
         */
//        t1.start();
        t2.start();
        t1.start();
    }
}
