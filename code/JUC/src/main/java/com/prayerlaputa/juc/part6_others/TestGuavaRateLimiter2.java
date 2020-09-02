package com.prayerlaputa.juc.part6_others;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/9/2
 */
public class TestGuavaRateLimiter2 {

    private static long prev = 0;

    public static void main(String[] args) throws Exception {

//限流器流速：2个请求/秒
        RateLimiter limiter = RateLimiter.create(2.0);
//执行任务的线程池
        ExecutorService es = Executors.newFixedThreadPool(1);
//记录上一次执行时间
        prev = System.nanoTime();
//测试执行20次
        for (int i=0; i<20; i++){
            //限流器限流
            limiter.acquire();
            //提交任务异步执行
            es.execute(()->{
                long cur=System.nanoTime();
                //打印时间间隔：毫秒
                System.out.println((cur-prev)/1000_000);
                prev = cur;
            });
        }

    }
}
