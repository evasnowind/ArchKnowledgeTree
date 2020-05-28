package com.prayerlaputa.juc.part4_referenceAndThreadLocal;

import java.io.IOException;

public class T01_NormalReference {
    public static void main(String[] args) throws IOException {
        //强引用：没有应用才会被回收
        M m = new M();
        m = null;
        System.gc(); //DisableExplicitGC
        //阻塞当前线程，以便观察是否执行了GC
        System.in.read();
    }
}
