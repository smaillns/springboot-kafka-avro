package com.example.demo.demokafka.consumer;

import com.example.demo.demokafka.event.MyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyKafkaConsumer {


    @KafkaListener (
            topics = "${app.kafka.my-consumer.topic.retry}",
            clientIdPrefix = "${app.kafka.my-consumer.client-id}",
            groupId = "${app.kafka.my-consumer.group-id}",
            containerFactory = "myRetryListenerFactory",
            autoStartup = "${app.kafka.my-consumer.enabled}"
    )
    @KafkaListener (
            topics = "${app.kafka.my-consumer.topic.main}",
            clientIdPrefix = "${app.kafka.my-consumer.client-id}",
            groupId = "${app.kafka.my-consumer.group-id}",
            containerFactory = "myListenerFactory",
            autoStartup = "${app.kafka.my-consumer.enabled}"
    )
    public void consumePaymentEvents(ConsumerRecord<String, MyEvent> consumerRecord) throws Exception {
        log.info("received event: {}", consumerRecord);
        throw new Exception("test");
    }

}
