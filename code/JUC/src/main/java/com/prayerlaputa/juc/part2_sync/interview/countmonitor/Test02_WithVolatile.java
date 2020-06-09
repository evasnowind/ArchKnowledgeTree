package com.prayerlaputa.juc.part2_sync.interview.countmonitor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu@100credit.com
 * created on 2020/5/15
 */
public class Test02_WithVolatile {

    /**
     * volatile的使用务必谨慎，不要轻易用。volatile修饰的变量，尽量是基本类型，不要修饰类似List这种引用类型，因为List内部变化、对象本身
     * 没有变化，volatile可能就观测不到。更严谨的说法：
     *      volatile关键字对于基本类型的修改可以在随后对多个线程的读保持一致，但是对于引用类型如数组，实体bean，仅仅保证引用的可见性，但并不保证引用内容的可见性。
     *      参考 https://blog.csdn.net/u010454030/article/details/80800098
     *
     * 参考https://blog.csdn.net/weixin_42008012/article/details/104673153
     *
     */
    volatile List list = new LinkedList();

//    volatile List list = Collections.synchronizedList(new LinkedList<>());

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public static void main(String[] args) {
        Test02_WithVolatile test = new Test02_WithVolatile();
        new Thread(
                () -> {
                    for (int i = 0; i < 10; i++) {
                        test.add(i);
                        System.out.println("add " + i);

                        //
                        /*
                        如果不sleep，test的变化就无法及时通知到t2线程。
                        原因应该是这样：
                        如果不sleep，线程t1、t2就都处于running状态，两者争抢CPU资源。
                        那么就无法保证，t1每循环一次后，t2立马执行、观察状态，也就不能及时执行、退出。
                        可能t1执行到5，t2刚好执行、看到队列是5、t2正要打印，结果t1又开始执行，。。。
                        所以不sleep的情况下，输出顺序不确定，甚至t2可能永远结束不了。
                        ——以上仅是个人推测，没去看源码，不敢肯定。

                        sleep一下，哪怕是很短时间，则可以保证t2能观察到t1状态。并且无论list是不是加锁的数据结构（如Collections.synchronizedList），都可以
                        保证程序正常执行
                         */
                        try {
//                            TimeUnit.SECONDS.sleep(1);
                            TimeUnit.NANOSECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
        , "t1").start();

        new Thread(() -> {
            while(true) {
                if (test.size() == 5) {
                    break;
                }
            }
            System.out.println("t2 结束");
        }, "t2").start();
    }
}
