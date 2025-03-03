package com.example.demo.demokafka.config;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

/**
 * This configuration provides the KafkaTemplate<String, GenericRecord> and ConsumerFactory<String, GenericRecord>
 * required by Tzatziki to use Kafka topics.
 */
@Configuration
public class KafkaTestConfig {

    @Bean
    public KafkaTemplate<String, GenericRecord> kafkaTemplate(ProducerFactory<String, GenericRecord> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, GenericRecord> producerFactory(
            @Value("${app.kafka.my-consumer.bootstrap-servers}") final String bootstrapServers,
            @Value("${app.kafka.my-consumer.schema-registry.url}") final String schemaRegistryUrl) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        configProps.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ConsumerFactory<String, GenericRecord> consumerFactory(
            @Value("${app.kafka.my-consumer.bootstrap-servers}") final String bootstrapServers,
            @Value("${app.kafka.my-consumer.schema-registry.url}") final String schemaRegistryUrl) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

        configProps.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

}