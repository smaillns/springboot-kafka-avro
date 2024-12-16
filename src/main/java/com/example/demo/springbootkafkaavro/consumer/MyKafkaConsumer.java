package com.example.demo.springbootkafkaavro.consumer;

import com.example.demo.springbootkafkaavro.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyKafkaConsumer {


    @Value("${app.kafka.topic}")
    private String topic;

    @KafkaListener(topics = "my_topic", groupId = "groupId")
    public void consumePaymentEvents(Event event)  {
        log.info("received event: {}", event);
    }

}
