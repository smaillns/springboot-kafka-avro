package com.example.demo.demokafka.config.kafka;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

import static com.example.demo.demokafka.common.utils.KafkaUtil.addExceptionHeaders;
import static com.example.demo.demokafka.common.utils.KafkaUtil.incrementRetryCountHeader;
import static org.apache.commons.collections4.IterableUtils.toList;


@Slf4j
@AllArgsConstructor
public class RetryConsumerRecordRecoverer<K, V> implements ConsumerRecordRecoverer {

	private String retryTopic;
	private KafkaTemplate<K, V> kafkaTemplate;
	private DeadLetterPublishingRecoverer deadLetterPublishingRecoverer;

	@Override
	public void accept(ConsumerRecord consumerRecord, Exception exception) {
		log.warn("[RetryConsumerRecordRecoverer] Exception received on record {}", consumerRecord.key(), exception);
		if (!isFatal(exception)) {
			try {
				sendRetryOrDlq(retryTopic, consumerRecord, exception);
			} catch (Exception sendingException) {
				log.warn("[RetryConsumerRecordRecoverer] Exception while sending the message to the retry topic", sendingException);
				sendToDlq(deadLetterPublishingRecoverer, consumerRecord, exception);
			}
		} else {
			log.warn("[RetryConsumerRecordRecoverer] Exception received is a fatal exception");
			sendToDlq(deadLetterPublishingRecoverer, consumerRecord, exception);
		}
	}

	/**
	 * send the message in the retry topic, adding the error message
	 *
	 * @param consumerRecord
	 */
	private void sendRetryOrDlq(String retryTopic, ConsumerRecord<K, V> consumerRecord, Throwable exception) {
		incrementRetryCountHeader(consumerRecord.headers());
		addExceptionHeaders(consumerRecord.headers(), exception);
		kafkaTemplate.send(new ProducerRecord<>(retryTopic, consumerRecord.partition(), consumerRecord.key(), consumerRecord.value(), toList(consumerRecord.headers())));
	}

	/**
	 * send the message in the dlt topic, adding the error message to headers
	 *
	 * @param deadLetterPublishingRecoverer
	 * @param consumerRecord
	 * @param exception
	 */
	private void sendToDlq(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer, ConsumerRecord<K, V> consumerRecord, @Nullable Exception exception) {
		if (exception == null) {
			return;
		}
		addExceptionHeaders(consumerRecord.headers(), exception);
		deadLetterPublishingRecoverer.accept(consumerRecord, exception);
	}

	private boolean isFatal(Throwable throwable) {
		return throwable instanceof SerializationException;
	}
}