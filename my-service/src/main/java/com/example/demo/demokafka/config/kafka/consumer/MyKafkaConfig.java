package com.example.demo.demokafka.config.kafka.consumer;


import com.example.demo.demokafka.config.kafka.CommonKafkaConfig;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;



@Configuration
@ConfigurationProperties(prefix = "app.kafka.my-consumer")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MyKafkaConfig extends CommonKafkaConfig<String, SpecificRecord> {

	@Autowired
	private KafkaProperties kafkaProperties;

	@Value ("${app.kafka.my-consumer.topic.retry}")
	private String retryTopic;

	@Value ("${app.kafka.my-consumer.topic.error}")
	private String dltTopic;

	@Bean
	public ConsumerFactory<String, SpecificRecord> specificConsumerFactory() {
		return consumerResource(kafkaProperties);
	}

	@Bean
	public ProducerFactory<String, SpecificRecord> specificProducerFactory() {
		return producerResource(kafkaProperties);
	}

	@Bean
	public KafkaTemplate<String, SpecificRecord> specificKafkaTemplate(ProducerFactory<String, SpecificRecord> specificProducerFactory) {
		return new KafkaTemplate<>(specificProducerFactory);
	}


	/******************************
	 * TOPIC CONFIGURATION
	 ******************************/
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, SpecificRecord> myListenerFactory(
			ConsumerFactory<String, SpecificRecord> specificConsumerFactory, KafkaTemplate<String, SpecificRecord> specificKafkaTemplate,
			DeadLetterPublishingRecoverer movementDeadLetter) {

		return retryKafkaListenerContainerFactory(specificConsumerFactory, retryTopic, specificKafkaTemplate, movementDeadLetter, fixedBackOffMain);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, SpecificRecord> myRetryListenerFactory(
			ConsumerFactory<String, SpecificRecord> specificConsumerFactory,
			DeadLetterPublishingRecoverer myDeadLetter) {

		return retryKafkaListenerContainerFactory(specificConsumerFactory, myDeadLetter, fixedBackOffRetry);
	}

	@Bean
	public DeadLetterPublishingRecoverer myDeadLetter(KafkaTemplate<String, SpecificRecord> specificKafkaTemplate) {
		return new DeadLetterPublishingRecoverer(specificKafkaTemplate,
				(consumerRecord, exception) -> new TopicPartition((dltTopic), consumerRecord.partition()));
	}

}