package com.prayerlaputa.juc.interview.blockqueue;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu
 * created on 2020/5/18
 */
public class Test02<T> {

    private LinkedList<T> lists = new LinkedList<>();

    private int MAX_CAPACITY = 10;

    private Lock lock = new ReentrantLock();

    private Condition producer = lock.newCondition();
    private Condition consumer = lock.newCondition();

    private volatile int count = 0;

    public void put(T t) {
        /*
        如果不加lock/unlock，直接使用condition，将导致这个异常：

        Exception in thread "Thread-1" Exception in thread "Thread-0" Exception in thread "Thread-2" java.lang.IllegalMonitorStateException
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.signalAll(AbstractQueuedSynchronizer.java:1954)
	at com.prayerlaputa.juc.interview.blockqueue.Test02.put(Test02.java:41)
	at com.prayerlaputa.juc.interview.blockqueue.Test02.lambda$main$0(Test02.java:71)
	at java.lang.Thread.run(Thread.java:748)
         */
        try {
            lock.lock();
            while (lists.size() == MAX_CAPACITY) {
                try {
                    producer.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            lists.add(t);
            count += 1;

            this.consumer.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }


    }

    public int getCount() {
        return count;
    }

    public T get() {
        T t = null;
        try {
            lock.lock();

            while (lists.size() == 0) {
                try {
                    consumer.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            count -= 1;
            t = lists.removeFirst();
            producer.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return t;
    }

    public static void main(String[] args) {
        Test02 test02 = new Test02();
        for (int i = 0; i < 10; i++) {
            final int tmp = i;
            Thread t = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    test02.put(j);
                    System.out.println("生产：" + tmp + " " + j + " count=" + test02.getCount());
                }
            }
            );
            t.start();
        }

//        try {
//            TimeUnit.SECONDS.sleep(2);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        for (int i = 0; i < 2; i++) {
            new Thread(() ->{
                for(int j = 0; j < 50; j++) {
                    System.out.println("消费：" + test02.get() + " count=" + test02.getCount());
                }

            }).start();
        }
    }
}
