package com.prayerlaputa.kafka;

import com.prayerlaputa.util.ConfigUtil;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * @author chenglong.yu
 * created on 2020/9/11
 */
public class KafkaProducerDemo extends Thread {
    private final KafkaProducer<Integer, String> producer;
    private final String topic;
    private final Boolean isAsync;
    private int numRecords;
    private final CountDownLatch latch;


    public KafkaProducerDemo(final String topic,
                             final Boolean isAsync,
                             final String transactionalId,
                             final boolean enableIdempotency,
                             final int numRecords,
                             final int transactionTimeoutMs,
                             CountDownLatch latch) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigUtil.getStringValue("kafkaServer.ip") + ":" + ConfigUtil.getStringValue("kafkaServer.port"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "ProducerDemo");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        if (transactionTimeoutMs > 0) {
            props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, transactionTimeoutMs);
        }
        if (transactionalId != null) {
            props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);
        }
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotency);

        this.producer = new KafkaProducer(props);
        this.topic = topic;
        this.isAsync = isAsync;
        this.numRecords = numRecords;
        this.latch = latch;
    }

    KafkaProducer<Integer, String> get() {
        return producer;
    }

    @Override
    public void run() {
        int messageKey = 0;
        int recordsSent = 0;
        while(recordsSent < numRecords) {
            String messageStr = "Message_" + messageKey;
            long startTime = System.currentTimeMillis();
            if (isAsync) {
                producer.send(new ProducerRecord(topic,
                        messageKey,
                        messageStr,
                        new DemoCallback(startTime, messageKey, messageStr)));
            } else {
                //同步发送
                try {
                    producer.send(new ProducerRecord<>(topic,
                            messageKey,
                            messageStr)).get();
                    System.out.println("Sent message: (" + messageKey + ", " + messageStr + ")");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            messageKey += 2;
            recordsSent += 1;
        }
        System.out.println("Producer sent " + numRecords + " records successfully");
        latch.countDown();
    }
}

class DemoCallback implements Callback {

    private final long startTime;
    private final int key;
    private final String message;

    DemoCallback(long startTime, int key, String message) {
        this.startTime = startTime;
        this.key = key;
        this.message = message;
    }


    /**
     * 消息被ACK时，才会调用该回调
     *
     * @param metadata
     * @param exception
     */
    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (metadata != null) {
            System.out.println(
                    "message(" + key + ", " + message + ") sent to partition(" + metadata.partition() +
                            "), " +
                            "offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
        } else {
            exception.printStackTrace();
        }
    }
}
