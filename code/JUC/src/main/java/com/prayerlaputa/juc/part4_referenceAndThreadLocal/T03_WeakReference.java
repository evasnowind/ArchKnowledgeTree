/**
 * 弱引用遭到gc就会回收
 *
 */
package com.prayerlaputa.juc.part4_referenceAndThreadLocal;

import java.lang.ref.WeakReference;

public class T03_WeakReference {
    public static void main(String[] args) {
        WeakReference<M> m = new WeakReference<>(new M());

        System.out.println(m.get());
        /*
        弱引用只要遭遇GC，就会回收
        作用：如果有另一个强引用指向弱引用时，只要强引用消失，此处的弱引用就应该被回收。
        结论：一般用在容器里
        典型应用：ThreadLocal
         */
        System.gc();
        System.out.println(m.get());


        ThreadLocal<M> tl = new ThreadLocal<>();
        tl.set(new M());
        tl.remove();

    }
}

