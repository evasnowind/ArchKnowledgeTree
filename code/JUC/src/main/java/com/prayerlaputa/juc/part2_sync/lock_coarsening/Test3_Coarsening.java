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
            System.out.println�ڲ�ʵ�����£�
    public void println(String x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }
            ���ڲ�����synchronized����

            ���ڴ˴�System.out.println������if����ڲ���������ÿ��ѭ��������м���������������
            ��˲��ᷢ�����ֻ�����������name��û�м�volatile���������߳���name�仯���ڵ�ǰ�̲߳��ɼ���
            ������߳̽�һֱ��ѭ����

            Ҳ���Դӷ��������������⣬�����򷴱����Ĵ������£�
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
