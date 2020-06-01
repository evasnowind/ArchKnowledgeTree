/**
 * ������
 * ����������������һЩ�����õ����Ǳ���Ķ���
 * ���������ù����ŵĶ�����ϵͳ��Ҫ�����ڴ�����쳣֮ǰ���������Щ�����н����շ�Χ���еڶ��λ��ա�
 * �����λ��ջ�û���㹻���ڴ棬�Ż��׳��ڴ�����쳣��
 * -Xmx20M
 */
package com.prayerlaputa.juc.part4_referenceAndThreadLocal;

import java.lang.ref.SoftReference;

public class T02_SoftReference {
    public static void main(String[] args) {
        //10M���ֽ�����
        SoftReference<byte[]> m = new SoftReference<>(new byte[1024*1024*10]);
        //m = null;
        System.out.println(m.get());
        System.gc();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(m.get());

        System.out.println("JVM �����=" + Runtime.getRuntime().maxMemory() + " �����ڴ�=" + Runtime.getRuntime().freeMemory());

        //�ٷ���һ�����飬heap��װ���£���ʱ��ϵͳ���������գ��Ȼ���һ�Σ������������������øɵ�
        int bLen = 1024*1024*15;
        System.out.println(bLen);

        //JVM�����������öѴ�СΪ20M����ʱ�ٷ���һ��15MB���飬heap��װ���£���ʱ��ϵͳ���������գ��Ȼ���һ�Σ������������������øɵ�
        byte[] b = new byte[bLen];
        //����֣��˴�����JVM���ڴ��СΪ20M��Ȼ��b����Ϊ15MB����OOM�ˡ���Ҫ��һ����ô���� TODO
        System.out.println(m.get());

        //�����÷ǳ��ʺϻ���ʹ��
    }
}


