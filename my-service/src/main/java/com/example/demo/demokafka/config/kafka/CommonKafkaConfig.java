package com.example.demo.demokafka.config.kafka;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

@Data
@Configuration
public class CommonKafkaConfig<K, V> {

	protected String bootstrapServers;
	protected String keyDeserializer;
	protected String valueDeserializer;
	protected int maxPollRecords;
	protected String autoOffsetReset;
	protected String groupId;
	protected SchemaRegistry schemaRegistry;
	protected Properties properties;
	protected FixedBackOff fixedBackOffMain;
	protected FixedBackOff fixedBackOffRetry;
	@Value ("${app.kafka.retry.topic.main.max}")
	private Long maxAttemptMainTopic;
	@Value ("${app.kafka.retry.topic.main.timems}")
	private Long intervalMainTopic;
	@Value ("${app.kafka.retry.topic.retry.max}")
	private Long maxAttemptRetryTopic;
	@Value ("${app.kafka.retry.topic.retry.timems}")
	private Long intervalRetryTopic;

	@PostConstruct
	private void postConstruct() {
		fixedBackOffMain = new FixedBackOff(intervalMainTopic, maxAttemptMainTopic);
		fixedBackOffRetry = new FixedBackOff(intervalRetryTopic, maxAttemptRetryTopic);
	}


	public ConsumerFactory<K, V> consumerResource(KafkaProperties kafkaProperties) {
		Map<String, Object> properties = kafkaProperties.buildConsumerProperties(null);
		properties.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.put(AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
		properties.put(KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
		properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
		properties.put(MAX_POLL_RECORDS_CONFIG, maxPollRecords);
		properties.put(GROUP_ID_CONFIG, groupId);
		buildRegistryProperties(properties);

		properties.put("spring.json.trusted.packages", this.properties.getJsonTrustedPackages());
		properties.put("spring.deserializer.key.delegate.class", this.properties.getDeserializerKeyDelegateClass());
		properties.put("spring.deserializer.value.delegate.class", this.properties.getDeserializerValueDelegateClass());
		properties.put(SPECIFIC_AVRO_READER_CONFIG, this.properties.isValueDeserializerSpecificAvroReader());
		properties.put(ENABLE_AUTO_COMMIT_CONFIG, false);

		return new DefaultKafkaConsumerFactory<>(properties);
	}

	public ProducerFactory<K, V> producerResource(KafkaProperties producerProperty) {
		Map<String, Object> producerProperties = producerProperty.buildProducerProperties(null);
		buildRegistryProperties(producerProperties);
		return new DefaultKafkaProducerFactory<>(producerProperties);
	}

	private void buildRegistryProperties(Map<String, Object> properties) {
		properties.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry.getUrl());
	}


	protected ConcurrentKafkaListenerContainerFactory<K, V> retryKafkaListenerContainerFactory(
		ConsumerFactory<K, V> consumerFactory,
		String retryTopic,
		KafkaTemplate<K, V> kafkaTemplate,
		DeadLetterPublishingRecoverer deadLetterPublishingRecoverer,
		FixedBackOff fixedBackOff) {
		ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(
			new DefaultErrorHandler(
				new RetryConsumerRecordRecoverer<>(retryTopic, kafkaTemplate, deadLetterPublishingRecoverer),
				fixedBackOff
			)
		);
		return factory;
	}

	protected ConcurrentKafkaListenerContainerFactory<K, V> retryKafkaListenerContainerFactory(
		ConsumerFactory<K, V> consumerFactory,
		DeadLetterPublishingRecoverer deadLetterPublishingRecoverer,
		FixedBackOff fixedBackOff) {
		ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(
			new DefaultErrorHandler(
				deadLetterPublishingRecoverer,
				fixedBackOff
			)
		);
		return factory;
	}


	@Data
	public static class SchemaRegistry {
		String url;
	}

	@Data
	public static class Properties {
		String jsonTrustedPackages;
		String deserializerKeyDelegateClass;
		String deserializerValueDelegateClass;
		boolean valueDeserializerSpecificAvroReader;
	}
}
