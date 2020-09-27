package com.prayerlaputa.juc.interview.countmonitor;

/**
 * @author chenglong.yu
 * created on 2020/6/8
 */
public class Test02_2_VolatileObj {

    private static volatile Data data;

    public static void setData(int a, int b) {
        data = new Data(a, b);
    }

    private static class Data {
        private int a;
        private int b;

        public Data(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }
    }

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 100000; i++) {
            int a = i;
            int b = i;
            //writer
            Thread writerThread = new Thread(() -> {
                for (int j = 0; j < 500; j++) {
                    setData(a, b);
                }
            });
            //reader
            Thread readerThread = new Thread(() -> {
                while (data == null) {
                }
                int x = data.getA();
                int y = data.getB();
                if (x != y) {
                    System.out.printf("a = %s, b = %s%n", x, y);
                }
            });

            writerThread.start();
            readerThread.start();
            writerThread.join();
            readerThread.join();
        }
        System.out.println("finished");
    }
}
