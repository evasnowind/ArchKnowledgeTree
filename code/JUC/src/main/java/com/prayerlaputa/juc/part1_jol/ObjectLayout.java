package com.prayerlaputa.juc.part1_jol;

import org.openjdk.jol.info.ClassLayout;
import sun.misc.Contended;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chenglong.yu
 * created on 2020/5/2
 */
public class ObjectLayout {

    public static void main(String[] args) throws Exception {

//        Object o = new Object();

        //延迟5s，因为JVM （至少在JVM 8）中，默认4s后才启动偏向锁，可以使用-XX:BiasedLockingStartupDelay 来配置启动时间
//        Thread.sleep(5000);
//
//        Object o = new Object();
//        System.out.println(ClassLayout.parseInstance(o).toPrintable());
//
//        SimpleObject simpleObject = new SimpleObject();
//        System.out.println(ClassLayout.parseInstance(simpleObject).toPrintable());


//        synchronized (o) {
//            System.out.println(ClassLayout.parseInstance(o).toPrintable());
//        }

//        PaddedAtomicLong paddedAtomicLong = new PaddedAtomicLong();
//        System.out.println(ClassLayout.parseInstance(paddedAtomicLong).toPrintable());
//        testPointer(paddedAtomicLong);


        ContendedPaddingLong contendedPaddingLong = new ContendedPaddingLong();
        System.out.println(ClassLayout.parseInstance(contendedPaddingLong).toPrintable());
    }



    private static void testPointer(PaddedAtomicLong pointer) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (long i = 0; i < 100000000; i++) {
                pointer.incrementAndGet();
            }
        });

        Thread t2 = new Thread(() -> {
            for (long i = 0; i < 100000000; i++) {
                //更换此处的变量，从p1到p6，查看所需时间。p6所需时间比较大
                pointer.p5++;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("cost time:" + (System.currentTimeMillis() - start));
        System.out.println("atomic counter=" + pointer.get());
        System.out.println("p1=" + pointer.p1);
        System.out.println("p2=" + pointer.p2);
        System.out.println("p3=" + pointer.p3);
        System.out.println("p4=" + pointer.p4);
        System.out.println("p5=" + pointer.p5);
    }
}

class SimpleObject extends Object {
    private int cnt;
}

class ContendedPaddingLong {
    /**
     * 默认使用这个@Contended注解是无效的，需要在JVM启动参数加上-XX:-RestrictContended才会生效。
     */
    @Contended
    public volatile long usefulVal;
    public volatile long anotherVal;
}

class PaddedAtomicLong extends AtomicLong {
    private static final long serialVersionUID = -3415778863941386253L;

    /** Padded 6 long (48 bytes) */
    public volatile long p1, p2, p3, p4, p5, p6 = 7L;

    /**
     * Constructors from {@link AtomicLong}
     */
    public PaddedAtomicLong() {
        super();
    }

    public PaddedAtomicLong(long initialValue) {
        super(initialValue);
    }

    /**
     * To prevent GC optimizations for cleaning unused padded references
     */
    public long sumPaddingToPreventOptimization() {
        return p1 + p2 + p3 + p4 + p5 + p6;
    }

}