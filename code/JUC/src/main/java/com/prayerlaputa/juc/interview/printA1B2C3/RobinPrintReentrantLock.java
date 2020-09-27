package com.prayerlaputa.juc.interview.printA1B2C3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu
 * created on 2020/9/24
 */
public class RobinPrintReentrantLock {
    private static final int TASK_NUM = 3;
    private static int num = 0;
    private static int flag = 0;
    private static Lock lock = new ReentrantLock();
    private static List<Condition> list = new ArrayList<Condition>();
    private static ExecutorService exec = Executors.newCachedThreadPool();

    static {
        for (int i = 0; i < TASK_NUM; i++) {
            list.add(lock.newCondition());
        }
    }

    private static void crit() {
        if (num >= 75) {
            System.exit(1);
        }
    }

    private static void print() {
        //打印前先判断一下，若num >= 75则程序退出
        crit();
        System.out.print(Thread.currentThread());
        //自增，然后打印
        System.out.format("%-2d ", ++num);
        System.out.println();
    }

    private static void work(int i) {
        while (!Thread.interrupted()) {
            lock.lock();
            try {
                /*
                本程序一共有3个线程，进入work方法时，
                i分别为0/1/2，而一开始flag=0.
                所以i=0会进入if，执行打印，并且flag=(i + 1) % list.size()，保证下次会由i=1线程执行；
                此时i=1/2会进入else分支、调用await，暂时阻塞。

                继续分析，当flag=(i + 1) % list.size()后，flag=1，
                此时会执行list.get(1).signal(), 也就是将上回被阻塞的i=1线程唤醒，此时i=0线程会通过while
                循环，继续执行，然而此时会走else分支，因此i=0会调用await方法、暂时阻塞；
                而此时i=1，从await()方法唤醒后，也是通过while，进入下次循环，此时将走if语句，
                执行print方法，然后再次flag=(i + 1) % list.size()。
                如此循环往复。
                 */
                if (flag == i) {
                    print();
                    flag = (i + 1) % list.size();
                    list.get(flag).signal();
                } else {
                    try {
                        list.get(i % list.size()).await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private static class Task implements Runnable {
        private final int i;

        public Task(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            work(i);
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        for (int i = 0; i < list.size(); i++) {
            //此处执行TASK_NUM=3个 任务
            exec.execute(new Task(i));
        }

    }
}
