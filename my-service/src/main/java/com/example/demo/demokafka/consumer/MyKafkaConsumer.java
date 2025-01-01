package com.example.demo.demokafka.consumer;

import com.example.demo.demokafka.event.MyEvent;
import com.example.demo.demokafka.model.MyModel;
import com.example.demo.demokafka.service.MyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyKafkaConsumer {


    private final MyService myService;

    @KafkaListener(
            topics = "${app.kafka.my-consumer.topic.retry}",
            clientIdPrefix = "${app.kafka.my-consumer.client-id}",
            groupId = "${app.kafka.my-consumer.group-id}",
            containerFactory = "myRetryListenerFactory",
            autoStartup = "${app.kafka.my-consumer.enabled}"
    )
    @KafkaListener(
            topics = "${app.kafka.my-consumer.topic.main}",
            clientIdPrefix = "${app.kafka.my-consumer.client-id}",
            groupId = "${app.kafka.my-consumer.group-id}",
            containerFactory = "myListenerFactory",
            autoStartup = "${app.kafka.my-consumer.enabled}"
    )
    public void consumePaymentEvents(ConsumerRecord<String, MyEvent> consumerRecord) {
        log.info("received event: {}", consumerRecord);
        var myEvent = consumerRecord.value();
        MyModel model = MyModel.builder().id(myEvent.getId()).label(myEvent.getLabel()).build(); // TODO USE A DEDICATED MAPPER
        myService.handleReceivedEvent(model);
    }

}
