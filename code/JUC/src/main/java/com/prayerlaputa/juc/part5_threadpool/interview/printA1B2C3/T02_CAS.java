package com.prayerlaputa.juc.part5_threadpool.interview.printA1B2C3;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/27
 */
public class T02_CAS {

    private static volatile int counter = 0;

    static Thread t1 = null, t2 = null;

    public static void main(String[] args) {
        char[] abcArr = "ABCDEF".toCharArray();
        char[] numArr = "123456".toCharArray();

        /**
         * 此处为CAS的写法。
         * 写CAS时，务必注意判断用于自旋的条件与自旋结束后业务逻辑执行完、初步下一个自旋的条件
         * 比如，下面代码如果写成
         * volatile int counter = 0
         * t1:
         * while(1 != counter){}
         * 业务逻辑
         * counter=1
         *
         * t2:
         * while(0 != counter){}
         * 业务逻辑
         * counter=0
         *
         * 将导致只能输出数字、输出字母死循环；
         * 关键就是判断条件务必要正确，基本就是业务逻辑完成后，对于变量的赋值，要保证下次会进入自旋
         *
         * 正确写法1：
         * volatile int counter = 0
         * t1:
         * while(1 == counter) {}
         * 业务逻辑
         * counter = 1;
         *
         * t2:
         * while(0 == counter) {}
         * 业务逻辑
         * counter = 0;
         *
         * 正确写法2：
         * volatile int counter = 0
         * t1:
         * while(0 != coutner) {}
         * 业务逻辑
         * counter = 1;
         *
         * t2:
         * while(1 != counter) {}
         * 业务逻辑
         * counter = 0;
         */
        t1 = new Thread(() -> {
            for (int i = 0; i < abcArr.length; i++) {
                while (0 != counter) {
                }
                System.out.print(abcArr[i]);
                counter = 1;
            }
        }, "t1");
        t2 = new Thread(() -> {
            for (int j = 0; j < numArr.length; j++) {
                while (1 != counter) {
                }
                System.out.print(numArr[j]);
                counter = 0;
            }

        }, "t2");

        t1.start();
        t2.start();
    }
}
