package com.prayerlaputa.kafka;

import com.prayerlaputa.util.ConfigUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * @author chenglong.yu
 * created on 2020/8/10
 */
public class KafkaConsumerDemo extends Thread {

    private final KafkaConsumer<Integer, String> consumer;
    private final String topic;
    private final String groupId;
    private final int numMessageToConsume;
    private int messageRemaining;
    private final CountDownLatch latch;

    public KafkaConsumerDemo(final String topic,
                             final String groupId,
                             final Optional<String> instanceId,
                             final boolean readCommitted,
                             final int numMessageToConsume,
                             final CountDownLatch latch) {


        this.groupId = groupId;


        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigUtil.getStringValue("kafkaServer.ip") + ":" + ConfigUtil.getStringValue("kafkaServer.port"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        instanceId.ifPresent(id -> props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, id));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.IntegerDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        if (readCommitted) {
            props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        }
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer(props);

        this.topic = topic;
        this.numMessageToConsume = numMessageToConsume;
        this.messageRemaining = numMessageToConsume;
        this.latch = latch;
    }

    KafkaConsumer<Integer, String> get() {
        return consumer;
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList(this.topic));
        ConsumerRecords<Integer, String> records = consumer.poll(Duration.ofSeconds(1));

        for (ConsumerRecord<Integer, String> record : records) {
            System.out.println(groupId + " received message : from partition " + record.partition() + ", (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
        }
        messageRemaining -= records.count();
        if (messageRemaining <= 0) {
            System.out.println(groupId + " finished reading " + numMessageToConsume + " messages");
            latch.countDown();
        }
    }

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "group1");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.records", 1000);
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList("test"));

        ConsumerRecords<String, String> msgList=consumer.poll(1000);
        int msgNo = 0;
        for (ConsumerRecord<String, String> record : msgList) {
            System.out.println(msgNo + "--------------" + record);
            if (msgNo % 1000 == 0) {
                break;
            }
            msgNo += 1;
        }
    }
}
