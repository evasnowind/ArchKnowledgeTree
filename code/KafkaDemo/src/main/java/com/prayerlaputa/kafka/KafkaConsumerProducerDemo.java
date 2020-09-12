package com.prayerlaputa.kafka;

import org.apache.kafka.common.errors.TimeoutException;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu
 * created on 2020/9/11
 */
public class KafkaConsumerProducerDemo {

    public static void main(String[] args) throws InterruptedException {
        boolean isAsync = args.length == 0 || !args[0].trim().equalsIgnoreCase("sync");

        String topic = "testTopic";

        CountDownLatch latch = new CountDownLatch(2);
//
//        KafkaProducerDemo producerThread = new KafkaProducerDemo(topic, isAsync, null, false, 10000, -1, latch);
//        producerThread.start();

        KafkaConsumerDemo consumerThread = new KafkaConsumerDemo(topic, "DemoConsumer", Optional.empty(), false, 10000, latch);
        consumerThread.start();

//        if (!latch.await(5, TimeUnit.MINUTES)) {
//            throw new TimeoutException("Timeout after 5 minutes waiting for demo producer and consumer to finish");
//        }

        System.out.println("All finished!");
    }

}
