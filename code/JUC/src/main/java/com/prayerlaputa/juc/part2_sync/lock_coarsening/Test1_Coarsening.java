package com.prayerlaputa.juc.part2_sync.lock_coarsening;

/**
 * @author chenglong.yu
 * created on 2020/8/28
 */
public class Test1_Coarsening implements Runnable {

    private static String name = "dog";

    @Override
    public void run() {
        while (true) {
            /*
            System.out.println内部实现如下：
    public void println(String x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }
            即内部存在synchronized锁。在while循环的情况下，会发生锁粗化，导致整歌while循环都在锁的范围内。
            因此name属性实际上每次都是从主线程内存拿数据、不存在可见性问题。

            反编译后的
              public void run()
              {
                for (;;)
                {
                  System.out.println(name);
                }
              }

             */
            System.out.println(name);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Test1_Coarsening test = new Test1_Coarsening();
        Thread thread = new Thread(test);
        thread.start();
        Thread.sleep(2000);
        Test1_Coarsening.name = "wangcai";
    }
}
