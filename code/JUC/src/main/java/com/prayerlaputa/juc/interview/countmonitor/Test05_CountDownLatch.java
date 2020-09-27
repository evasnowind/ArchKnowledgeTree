package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/5/16
 */
public class Test05_CountDownLatch {


    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());



    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test05_CountDownLatch test = new Test05_CountDownLatch();

        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            if (test.size() != 5) {
                try {
                    latch.await();
                    System.out.println("t2 ִ��");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("t2 ����");
        }, "t2").start();


        new Thread(
                () -> {
                    for (int i = 0; i < 10; i++) {
                        test.add(i);
                        System.out.println("add " + i);

                        if (test.size() == 5) {
                            latch.countDown();
                        }

                        /*
                        �������ߣ�ֻ��countDown()һ�£�t1������ռ��CPUʱ�䣬t2�޷����CPUʱ�䣬�޷�ִ�С�
                        ����ִ�н������t2��Ȼ�Ѿ��õ���latch��������ֻ��t1��΢�ó���ʱ�䣬t2���ܴ�ӡ�����t2ִ�С���
                        ���ִ��ʱ�䲻һ����������t1ִ����ż���ִ��t2��Ҳ������t1ָ������8����...������������������û��
                        �ﵽԤ�ڣ���Ϊû�о�ȷ�ڵ�5��ʱ׼��ִ�С�
                        �����ϸ�����������д�������⡣
                        ���Ҫ��֤�ڵ�5��ʱ�򣬾�ȷ��ӡ����Ҫ������CountDownLatch���ֱ������һ�¡�������notify/wait
                        �����һ���ĵ���

                        ���򵥵�д������LockSupport.park/unpark
                         */
                        try {
//                            TimeUnit.SECONDS.sleep(1);
                            TimeUnit.NANOSECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                , "t1").start();
    }
}
