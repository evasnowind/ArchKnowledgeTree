package com.prayerlaputa.juc.part2_sync.lock_coarsening;

/**
 * @author chenglong.yu
 * created on 2020/8/28
 */
public class Test2_Coarsening implements Runnable {

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
            ��˵����߳���name���Է����仯ʱ���˴���name���ܸ�֪������˻�����ѭ����

            ��ǰ���뷴��������£�
              public void run()
              {
                while (!"wangcai".equals(name)) {
                  System.out.println("not wangcai!");
                }
              }

             */
            if ("wangcai".equals(name)) {
                break;
            }
            System.out.println("not wangcai!");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Test2_Coarsening test = new Test2_Coarsening();
        Thread thread = new Thread(test);
        thread.start();
        Thread.sleep(2000);
        Test2_Coarsening.name = "wangcai";
    }
}
