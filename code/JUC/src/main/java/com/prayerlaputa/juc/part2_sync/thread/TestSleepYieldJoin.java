package com.prayerlaputa.juc.part2_sync.thread;

/**
 * @author chenglong.yu
 * created on 2020/5/2
 */
public class TestSleepYieldJoin {
    public static void main(String[] args) {
        //testSleep();
        testYield();
//        testJoin();
    }
    /*Sleep,意思就是睡眠，当前线程暂停一段时间让给别的线程去运行。Sleep是怎么复活的？由你的睡眠时间而定，等睡眠到规定的时间自动复活*/
    static void testSleep() {
        new Thread(()->{
            for(int i=0; i<100; i++) {
                System.out.println("A" + i);
                try {
                    Thread.sleep(500);
                    //TimeUnit.Milliseconds.sleep(500)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /*Yield,就是当前线程正在执行的时候停止下来进入等待队列，回到等待队列里在系统的调度算法里头呢还是依然有可能把你刚回去的这个线程拿回来继续执行，当然，更大的可能性是把原来等待的那些拿出一个来执行，所以yield的意思是我让出一下CPU，后面你们能不能抢到那我不管*/
    static void testYield() {
        new Thread(()->{
            for(int i=0; i<100; i++) {
                System.out.println("A" + i);
                if(i%10 == 0) Thread.yield();


            }
        }).start();

        new Thread(()->{
            for(int i=0; i<100; i++) {
                System.out.println("------------B" + i);
                if(i%10 == 0) Thread.yield();
            }
        }).start();
    }
    /*join， 意思就是在自己当前线程加入你调用Join的线程（），本线程等待。等调用的线程运行完了，自己再去执行。t1和t2两个线程，在t1的某个点上调用了t2.join,它会跑到t2去运行，t1等待t2运行完毕继续t1运行（自己join自己没有意义） */
    static void testJoin() {
        Thread t1 = new Thread(()->{
            for(int i=0; i<100; i++) {
                System.out.println("A" + i);
                try {
                    Thread.sleep(500);
                    //TimeUnit.Milliseconds.sleep(500)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(()->{

            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(int i=0; i<100; i++) {
                System.out.println("A" + i);
                try {
                    Thread.sleep(500);
                    //TimeUnit.Milliseconds.sleep(500)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();
    }
}
