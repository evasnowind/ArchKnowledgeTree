/**
 * 假设你能够提供一个服务
 * 这个服务查询各大电商网站同一类产品的价格并汇总展示
 * @author 马士兵 http://mashibing.com
 */

package com.prayerlaputa.juc.part5_threadpool.demo;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class T06_01_CompletableFuture {
    /**
     * 本程序意图：模拟从taobao tianmao jingdong 抓取同样商品价格、然后比较
     * 抓取结果都拿到之后才往下走
     *
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        long start, end;

        /*start = System.currentTimeMillis();

        priceOfTM();
        priceOfTB();
        priceOfJD();

        end = System.currentTimeMillis();
        System.out.println("use serial method call! " + (end - start));*/

        start = System.currentTimeMillis();

        CompletableFuture<Double> futureTM = CompletableFuture.supplyAsync(()->priceOfTM());
        CompletableFuture<Double> futureTB = CompletableFuture.supplyAsync(()->priceOfTB());
        CompletableFuture<Double> futureJD = CompletableFuture.supplyAsync(()->priceOfJD());

        /*
        CompletableFuture 任务的管理类
        allOf ： 提供了对一批任务的管理，等所有任务完成后才往下走。
        其他：anyOf,
         */
        CompletableFuture.allOf(futureTM, futureTB, futureJD).join();

        //lambda 表达式应用
//        CompletableFuture.supplyAsync(()->priceOfTM())
//                .thenApply(String::valueOf)
//                .thenApply(str-> "price " + str)
//                .thenAccept(System.out::println);


        end = System.currentTimeMillis();
        System.out.println("use completable future! " + (end - start));

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double priceOfTM() {
        delay();
        return 1.00;
    }

    private static double priceOfTB() {
        delay();
        return 2.00;
    }

    private static double priceOfJD() {
        delay();
        return 3.00;
    }

    /*private static double priceOfAmazon() {
        delay();
        throw new RuntimeException("product not exist!");
    }*/

    private static void delay() {
        int time = new Random().nextInt(500);
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("After %s sleep!\n", time);
    }
}
