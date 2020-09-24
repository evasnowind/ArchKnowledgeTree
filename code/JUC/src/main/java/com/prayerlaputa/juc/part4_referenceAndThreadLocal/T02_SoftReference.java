/**
 * 软引用
 * 软引用是用来描述一些还有用但并非必须的对象。
 * 对于软引用关联着的对象，在系统将要发生内存溢出异常之前，将会把这些对象列进回收范围进行第二次回收。
 * 如果这次回收还没有足够的内存，才会抛出内存溢出异常。
 * -Xmx20M
 */
package com.prayerlaputa.juc.part4_referenceAndThreadLocal;

import java.lang.ref.SoftReference;

public class T02_SoftReference {
    public static void main(String[] args) {
        //10M的字节数组
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

        System.out.println("JVM 堆最大=" + Runtime.getRuntime().maxMemory() + " 空闲内存=" + Runtime.getRuntime().freeMemory());

        //再分配一个数组，heap将装不下，这时候系统会垃圾回收，先回收一次，如果不够，会把软引用干掉
        int bLen = 1024*1024*15;
        System.out.println(bLen);

        //JVM启动参数设置堆大小为20M，此时再分配一个15MB数组，heap将装不下，这时候系统会垃圾回收，先回收一次，如果不够，会把软引用干掉
        byte[] b = new byte[bLen];
        //好奇怪，此处设置JVM堆内存大小为20M，然后b设置为15MB，报OOM了。需要查一下怎么回事 TODO
        System.out.println(m.get());

        //软引用非常适合缓存使用
    }
}


