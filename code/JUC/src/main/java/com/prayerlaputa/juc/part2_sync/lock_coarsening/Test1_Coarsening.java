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
            System.out.println�ڲ�ʵ�����£�
    public void println(String x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }
            ���ڲ�����synchronized������whileѭ��������£��ᷢ�����ֻ�����������whileѭ���������ķ�Χ�ڡ�
            ���name����ʵ����ÿ�ζ��Ǵ����߳��ڴ������ݡ������ڿɼ������⡣

            ��������
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
