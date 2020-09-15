package com.prayerlaputa.juc.part6_others.thread_interrupt;

/**
 * @author chenglong.yu
 * created on 2020/9/15
 */
public class ThreadInterrupt2 {
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
num : 100     时间：154
num : 200     时间：308
num : 300     时间：467
num : 400     时间：620
num : 500     时间：797
num : 600     时间：950
num : 700     时间：1100
num : 800     时间：1256
num : 900     时间：1411
num : 1000     时间：1565
num : 1100     时间：1719
num : 1200     时间：1871
num : 1300     时间：2031
num : 1400     时间：2187
num : 1500     时间：2338
num : 1600     时间：2492
num : 1700     时间：2647
num : 1800     时间：2804
num : 1900     时间：2960
执行中断     时间：3000
java.lang.InterruptedException: sleep interrupted
	at java.lang.Thread.sleep(Native Method)
	at com.prayerlaputa.juc.part6_others.thread_interrupt.ThreadInterrupt2$Thread1.run(ThreadInterrupt2.java:70)
num : 2000     时间：3120
num : 2100     时间：3271
num : 2200     时间：3422
num : 2300     时间：3580
.......

一直在执行，没有结束。
         */
        test2();
    }

    private static void test2() {

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

                //sleep一下
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    /*
                    静态代码扫描工具，比如sonar，会提示，如果捕获到异常信息，
                    当发生InterruptedException异常时，将会重置interrupt标志位，因此sonar建议：
                    1、重新抛出异常
                    2、即将线程中断标志位手工置位

InterruptedExceptions should never be ignored in the code, and simply logging the exception counts in this case as "ignoring". The throwing of the InterruptedException clears the interrupted state of the Thread, so if the exception is not handled properly the fact that the thread was interrupted will be lost. Instead, InterruptedExceptions should either be rethrown - immediately or after cleaning up the method's state - or the thread should be re-interrupted by calling Thread.interrupt() even if this is supposed to be a single-threaded application. Any other course of action risks delaying thread shutdown and loses the information that the thread was interrupted - probably without finishing its task.

Similarly, the ThreadDeath exception should also be propagated. According to its JavaDoc:

If ThreadDeath is caught by a method, it is important that it be rethrown so that the thread actually dies.

Noncompliant Code Example
public void run () {
  try {
    while (true) {
      // do stuff
    }
  }catch (InterruptedException e) { // Noncompliant; logging is not enough
    LOGGER.log(Level.WARN, "Interrupted!", e);
  }
}

Compliant Solution
public void run () {
  try {
    while (true) {
      // do stuff
    }
  }catch (InterruptedException e) {
    LOGGER.log(Level.WARN, "Interrupted!", e);
    // Restore interrupted state...
    Thread.currentThread().interrupt();
  }
}

参见https://blog.csdn.net/hesong1120/article/details/79164445
                     */
                }

                if (num % 100 == 0) {
                    printContent("num : " + num);
                }
            }
        }
    }
}
