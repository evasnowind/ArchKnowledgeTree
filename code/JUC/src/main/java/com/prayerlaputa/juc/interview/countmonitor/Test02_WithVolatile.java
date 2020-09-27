package com.prayerlaputa.juc.interview.countmonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/5/15
 */
public class Test02_WithVolatile {

    /**
     * volatile��ʹ����ؽ�������Ҫ�����á�volatile���εı����������ǻ������ͣ���Ҫ��������List�����������ͣ���ΪList�ڲ��仯��������
     * û�б仯��volatile���ܾ͹۲ⲻ�������Ͻ���˵����
     *      volatile�ؼ��ֶ��ڻ������͵��޸Ŀ��������Զ���̵߳Ķ�����һ�£����Ƕ����������������飬ʵ��bean��������֤���õĿɼ��ԣ���������֤�������ݵĿɼ��ԡ�
     *      �ο� https://blog.csdn.net/u010454030/article/details/80800098
     *
     * �ο�https://blog.csdn.net/weixin_42008012/article/details/104673153
     *
     */
    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test02_WithVolatile test = new Test02_WithVolatile();
        new Thread(
                () -> {
                    for (int i = 0; i < 10; i++) {
                        test.add(i);
                        System.out.println("add " + i);

                        //
                        /*
                        �����sleep��test�ı仯���޷���ʱ֪ͨ��t2�̡߳�
                        ԭ��Ӧ����������
                        �����sleep���߳�t1��t2�Ͷ�����running״̬����������CPU��Դ��
                        ��ô���޷���֤��t1ÿѭ��һ�κ�t2����ִ�С��۲�״̬��Ҳ�Ͳ��ܼ�ʱִ�С��˳���
                        ����t1ִ�е�5��t2�պ�ִ�С�����������5��t2��Ҫ��ӡ�����t1�ֿ�ʼִ�У�������
                        ���Բ�sleep������£����˳��ȷ��������t2������Զ�������ˡ�
                        �������Ͻ��Ǹ����Ʋ⣬ûȥ��Դ�룬���ҿ϶���

                        sleepһ�£������Ǻܶ�ʱ�䣬����Ա�֤t2�ܹ۲쵽t1״̬����������list�ǲ��Ǽ��������ݽṹ����Collections.synchronizedList����������
                        ��֤��������ִ��
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

        new Thread(() -> {
            while(true) {
                if (test.size() == 5) {
                    break;
                }
            }
            System.out.println("t2 ����");
        }, "t2").start();
    }
}
