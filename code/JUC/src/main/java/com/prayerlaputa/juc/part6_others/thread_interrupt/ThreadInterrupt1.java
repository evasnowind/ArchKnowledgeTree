package com.prayerlaputa.juc.part6_others.thread_interrupt;

/**
 * @author chenglong.yu
 * created on 2020/9/15
 */
public class ThreadInterrupt1 {
    //这里用来打印消耗的时间
    private static long time = 0;

    private static void resetTime() {
        time = System.currentTimeMillis();
    }

    private static void printContent(String content) {
        System.out.println(content + "     时间：" + (System.currentTimeMillis() - time));
    }

    public static void main(String[] args) {
        /*
        输出如下：
       .......
num : 11986500     时间：2999
num : 11986600     时间：2999
num : 11986700     时间：2999
num : 11986800     时间：2999
num : 11986900     时间：2999
num : 11987000     时间：2999
num : 11987100     时间：2999
执行中断     时间：3000
当前线程 isInterrupted     时间：3001
         */
        test1();
    }

    private static void test1() {

        Thread1 thread1 = new Thread1();
        thread1.start();

        //延时3秒后interrupt中断
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        thread1.interrupt();
        printContent("执行中断");
    }

    private static class Thread1 extends Thread {

        @Override
        public void run() {

            resetTime();

            int num = 0;
            while (true) {
                if (isInterrupted()) {
                    printContent("当前线程 isInterrupted");
                    break;
                }

                num++;

                if (num % 100 == 0) {
                    printContent("num : " + num);
                }
            }
        }
    }
}
