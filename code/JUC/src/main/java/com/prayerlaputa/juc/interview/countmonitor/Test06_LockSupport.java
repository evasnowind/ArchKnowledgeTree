package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * @author chenglong.yu
 * created on 2020/5/16
 */
public class Test06_LockSupport {

    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test06_LockSupport test = new Test06_LockSupport();

        final Object lock = new Object();

        Thread t1 = null;
        Thread t2 = new Thread(() -> {
            System.out.println("t2 ִ��");
            if (test.size() != 5) {
                LockSupport.park();
            }
            System.out.println("t2 ����");
//            LockSupport.unpark(t1);
        }, "t2");
        t2.start();
        //�˴�t2��ִ�к���Ҫ

        t1 = new Thread(
                () -> {
                    for (int i = 0; i < 10; i++) {
                        test.add(i);
                        System.out.println("add " + i);

                        if (test.size() == 5) {
                            LockSupport.unpark(t2);
                        }

                        /*
                        �������ߣ�t1������ռ��CPUʱ�䣬t2�޷����CPUʱ�䣬�޷�ִ�С�
                        ����ִ�н������t2��Ȼ�Ѿ��õ���������ֻ��t1��΢�ó���ʱ�䣬t2���ܴ�ӡ�����t2ִ�С���
                        ���ִ��ʱ�䲻һ����������t1ִ����ż���ִ��t2��Ҳ������t1ָ������8����...������������������û��
                        �ﵽԤ�ڣ���Ϊû�о�ȷ�ڵ�5��ʱ׼��ִ�С�
                         */
//                        try {
////                            TimeUnit.SECONDS.sleep(1);
//                            TimeUnit.NANOSECONDS.sleep(1);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }

                    }

                }
                , "t1");
        t1.start();
    }
}
