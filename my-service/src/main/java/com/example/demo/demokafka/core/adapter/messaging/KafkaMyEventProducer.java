package com.example.demo.demokafka.core.adapter.messaging;

import com.example.demo.demokafka.config.kafka.producer.SpecificRecordSenderImpl;
import com.example.demo.demokafka.core.domain.model.MyModel;
import com.example.demo.demokafka.core.port.MyEventProducer;
import com.example.demo.demokafka.event.MyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMyEventProducer implements MyEventProducer {

    private final SpecificRecordSenderImpl specificRecordSender;

    @Value("${app.kafka.my-consumer.topic.output}")
    private String outputTopic;


    @Override
    public void publishOutputEvent(MyModel myModel) {
        MyEvent outputEvent = myModel.toEvent();
        ProducerRecord<String, SpecificRecord> producerRecord = new ProducerRecord<>(
                outputTopic,
                0,
                "",
                outputEvent,
                Collections.emptyList());
        specificRecordSender.sendToKafka(producerRecord);
    }

}
