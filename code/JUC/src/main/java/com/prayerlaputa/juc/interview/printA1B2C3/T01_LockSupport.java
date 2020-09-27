package com.prayerlaputa.juc.interview.printA1B2C3;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * @author chenglong.yu
 * created on 2020/5/27
 */
public class T01_LockSupport {

    static Thread t1 = null, t2 =null;

    public static void main(String[] args) {

        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /*
         * Ϊ�˿��ƴ�ӡ˳�������̷ֱ߳���Է�������������
         * �˴��и�ϸ�ڣ�print��λ�þ���������ַ��������֡���ĸ���Ⱥ�˳��
         *
         */
        t1 = new Thread(() -> {
            for (int i = 0; i < numArr.length; i++) {
                //park������ס���ǵ�ǰ�̣߳��������park(obj)��Ҳ����������obj��Ϊblocker���μ�LockSupportԴ�롣
                LockSupport.park();
                System.out.print(numArr[i]);
                //unpark�������봫����Ҫ�������߳�
                LockSupport.unpark(t2);
            }
        }, "t1");
        t2 = new Thread(() -> {
            for (int i = 0; i < abcArr.length; i++) {
                //�ȴ�ӡ��unpark��������unpark�ٴ�ӡ�������Ա�֤�������ĸ���������
//                System.out.print(abcArr[i]);
                LockSupport.unpark(t1);
                System.out.print(abcArr[i]);
                //��Ҫ��������һ�����
                LockSupport.park();
                //������������������֡�Ȼ����ĸ���ַ���
//                System.out.print(abcArr[i]);
            }
        });

        //t1/t2˭����������Ӱ�죬��Ϊ��Ҫ�ǿ�park���������˳��
        t2.start();
        t1.start();
    }
}
