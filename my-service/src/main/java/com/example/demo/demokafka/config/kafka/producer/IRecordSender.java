package com.example.demo.demokafka.config.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@FunctionalInterface
public interface IRecordSender<k, v> {
	Logger log = LoggerFactory.getLogger(IRecordSender.class);

	void sendToKafka(ProducerRecord<k, v> producerRecord);

	default void sendRecord(KafkaTemplate<k, v> KafkaTemplate, ProducerRecord<k, v> producerRecord) {
		CompletableFuture<SendResult<k, v>> future = KafkaTemplate.send(producerRecord);
		try {
			future.get();
		} catch (ExecutionException e) {
			log.warn("Error when sending message {} into {} : ", producerRecord.value(), producerRecord.topic(), e);
			// throw new EyesKafkaException(producerRecord.value().toString(), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Error when sending message {} into {} : ", producerRecord.value(), producerRecord.topic(), e);
			// throw new EyesKafkaException(producerRecord.value().toString(), e);
		}
		log.info("Successfully sent message : {} into {}", producerRecord.value(), producerRecord.topic());
	}
}