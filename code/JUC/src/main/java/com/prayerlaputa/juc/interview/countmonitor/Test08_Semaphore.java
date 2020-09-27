package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author chenglong.yu
 * created on 2020/5/16
 */
public class Test08_Semaphore {


    volatile List list = new LinkedList();

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    static Thread t1 = null, t2 = null;

    public static void main(String[] args) {
        Test08_Semaphore test = new Test08_Semaphore();

        Semaphore s = new Semaphore(1);

        t1 = new Thread(() -> {
            try {
                s.acquire();
                for (int i = 0; i < 5; i++) {
                    test.add(new Object());
                    System.out.println("add " + i);


                }
                s.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                /*
                ���������ź����Ľⷨ��ʵ���ϸ���ĿҪ����Щ��㣣���Ϊ����ֻ���ж�=5���������������
                ѭ���Ͳ���˵�ˡ��˴�ֻ��Ϊ��չʾ���ź���д����
                Ϊ�˱�֤t1��ִ�У�t2��t1ִ�к�ſ�ʼstart
                 */
                t2.start();
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                s.acquire();
                for (int i = 5; i < 10; i++) {
                    System.out.println(i);
                }
                s.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, "t1");

        t2 = new Thread(() -> {
            try {
                s.acquire();
                System.out.println("t2 ����");
                s.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t2");

        //t2.start();
        t1.start();
    }
}
