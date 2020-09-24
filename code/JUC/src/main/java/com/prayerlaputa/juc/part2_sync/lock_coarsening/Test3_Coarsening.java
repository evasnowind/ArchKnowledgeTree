package com.prayerlaputa.juc.part2_sync.lock_coarsening;

/**
 * @author chenglong.yu
 * created on 2020/8/28
 */
public class Test3_Coarsening implements Runnable {

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
            即内部存在synchronized锁。

            由于此处System.out.println方法在if语句内部，并不是每次循环都会进行加锁、解锁操作，
            因此不会发生锁粗化，并且由于name并没有加volatile，导致主线程中name变化对于当前线程不可见。
            因此子线程将一直死循环。

            也可以从反编译代码着手理解，本程序反编译后的代码如下：
                 public void run()
                  {
                    while (!"wangcai".equals(name)) {}
                    System.out.println("not wangcai!");
                  }
             */
            if ("wangcai".equals(name)) {
                System.out.println("not wangcai!");
                break;
            }

        }
    }

    public static void main(String[] args) throws InterruptedException {
        Test3_Coarsening test = new Test3_Coarsening();
        Thread thread = new Thread(test);
        thread.start();
        Thread.sleep(2000);
        Test3_Coarsening.name = "wangcai";
    }
}
