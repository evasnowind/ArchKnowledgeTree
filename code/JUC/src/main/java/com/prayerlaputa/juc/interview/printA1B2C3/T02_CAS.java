package com.prayerlaputa.juc.interview.printA1B2C3;

/**
 * @author chenglong.yu
 * created on 2020/5/27
 */
public class T02_CAS {

    private static volatile int counter = 0;

    static Thread t1 = null, t2 = null;

    public static void main(String[] args) {
        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /**
         * �˴�ΪCAS��д����
         * дCASʱ�����ע���ж���������������������������ҵ���߼�ִ���ꡢ������һ������������
         * ���磬����������д��
         * volatile int counter = 0
         * t1:
         * while(1 != counter){}
         * ҵ���߼�
         * counter=1
         *
         * t2:
         * while(0 != counter){}
         * ҵ���߼�
         * counter=0
         *
         * ������ֻ��������֡������ĸ��ѭ����
         * �ؼ������ж��������Ҫ��ȷ����������ҵ���߼���ɺ󣬶��ڱ����ĸ�ֵ��Ҫ��֤�´λ��������
         *
         * ��ȷд��1��
         * volatile int counter = 0
         * t1:
         * while(1 == counter) {}
         * ҵ���߼�
         * counter = 1;
         *
         * t2:
         * while(0 == counter) {}
         * ҵ���߼�
         * counter = 0;
         *
         * ��ȷд��2��
         * volatile int counter = 0
         * t1:
         * while(0 != coutner) {}
         * ҵ���߼�
         * counter = 1;
         *
         * t2:
         * while(1 != counter) {}
         * ҵ���߼�
         * counter = 0;
         */
        t1 = new Thread(() -> {
            for (int i = 0; i < abcArr.length; i++) {
                while (0 != counter) {
                }
                System.out.print(abcArr[i]);
                counter = 1;
            }
        }, "t1");
        t2 = new Thread(() -> {
            for (int j = 0; j < numArr.length; j++) {
                while (1 != counter) {
                }
                System.out.print(numArr[j]);
                counter = 0;
            }

        }, "t2");

        t1.start();
        t2.start();
    }
}
