package com.prayerlaputa.juc.part4_referenceAndThreadLocal;

public class M {
    @Override
    protected void finalize() throws Throwable {
        System.out.println("finalize");
    }
}
