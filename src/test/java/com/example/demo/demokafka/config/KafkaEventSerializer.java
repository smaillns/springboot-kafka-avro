package com.example.demo.demokafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class KafkaEventSerializer implements Serializer<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String topic, Object event) {
        try {
            if (event == null) {
                throw new SerializationException("Can't serialize null");
            }
            objectMapper.findAndRegisterModules();
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new SerializationException("Error when serializing event to byte[]");
        }
    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
