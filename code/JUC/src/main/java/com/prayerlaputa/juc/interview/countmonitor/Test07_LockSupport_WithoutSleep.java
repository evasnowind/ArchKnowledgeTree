package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * @author chenglong.yu
 * created on 2020/5/16
 */
public class Test07_LockSupport_WithoutSleep {


    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());


    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    static Thread t1 = null, t2 = null;

    public static void main(String[] args) {
        Test07_LockSupport_WithoutSleep test = new Test07_LockSupport_WithoutSleep();

        t1 = new Thread(() -> {
            System.out.println("t1����");
            for (int i = 0; i < 10; i++) {
                test.add(new Object());
                System.out.println("add " + i);

                if (test.size() == 5) {
                    LockSupport.unpark(t2);
                    /*
                    �˴�t1����park����Ϊt1����ͣһ�£�������t2ִ��ʱ�䲻�ɿأ�
                    ��ΪCPUʱ�䱻t1ռ����
                     */
                    LockSupport.park();
                }
            }
        }, "t1");

        t2 = new Thread(() -> {
            System.out.println("t2����");
            //if (c.size() != 5) {
            LockSupport.park();
            //}
            System.out.println("t2 ����");
            LockSupport.unpark(t1);
        }, "t2");

        t2.start();
        t1.start();
    }
}
