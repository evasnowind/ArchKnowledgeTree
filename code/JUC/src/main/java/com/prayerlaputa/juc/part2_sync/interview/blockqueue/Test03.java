package com.prayerlaputa.juc.part2_sync.interview.blockqueue;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenglong.yu@brgroup.com
 * created on 2020/7/8
 */
public class Test03 <T>{

    private LinkedList<T> blockingList = new LinkedList<>();


    private int MAX_CAPACITY = 10;

    private Lock lock = new ReentrantLock();

    private Condition producer = lock.newCondition();
    private Condition consumer = lock.newCondition();

    public void put(T t) {
        lock.lock();
        try {
            while(blockingList.size() == MAX_CAPACITY) {
                producer.await();
            }

            blockingList.add(t);
            consumer.signalAll();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

    public T get(T t) {

        T obj = null;
        lock.lock();
        try {
            while (blockingList.size() == 0) {
                consumer.await();
            }
            obj = blockingList.removeFirst();
            producer.signalAll();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}
