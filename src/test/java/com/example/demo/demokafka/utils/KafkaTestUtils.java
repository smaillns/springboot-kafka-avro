package com.example.demo.demokafka.utils;

import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

public class KafkaTestUtils {

    private final EmbeddedKafkaBroker embeddedKafkaBroker;
    private KafkaMessageListenerContainer<String, GenericRecord> container;
    private BlockingQueue<ConsumerRecord<String, GenericRecord>> records;


    public KafkaTestUtils(EmbeddedKafkaBroker embeddedKafkaBroker) {
        this.embeddedKafkaBroker = embeddedKafkaBroker;
    }


    /**
     * Register the schema derived from the avro generated class for the given topic.
     *
     * @param schemaId the schema id to use
     * @param topic    the topic name for the message schema to register
     * @param schema   the schema JSON string
     * @throws Exception if there is an error during schema registration
     */
    public void registerSchema(int schemaId, String topic, String schema) throws Exception {
        // Stub for the POST of the subject, to return the associated schemaId.
        // (The Avro schema, obtained by the serializer by reflection, will be in the body POSTed).
        // This is used by the Producer when serializing.
        stubFor(post(urlEqualTo("/subjects/" + topic + "-value/versions?normalize=false"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"id\":" + schemaId + "}")));

        // Stub for the GET registered schema call for the given schema Id, returning the schema.
        // This is used by the Consumer when deserializing.
        final SchemaString schemaString = new SchemaString(schema);
        stubFor(get(urlPathMatching("/schemas/ids/" + schemaId))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(schemaString.toJson())));
    }



    /**
     * Sets up a Kafka consumer for the specified topic and schema registry URL.
     *
     * @param topic             the topic to consume from
     * @param schemaRegistryUrl the URL of the schema registry
     */
    public void setupConsumer(String topic, String schemaRegistryUrl) {
        DefaultKafkaConsumerFactory<String, GenericRecord> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties(schemaRegistryUrl));
        ContainerProperties containerProperties = new ContainerProperties(topic);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, GenericRecord>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    /**
     * Polls an event from the Kafka consumer within the specified timeout.
     *
     * @param timeoutMillis the timeout in milliseconds to wait for an event
     * @return the polled ConsumerRecord, or null if the timeout is reached
     * @throws InterruptedException if interrupted while waiting
     */
    public ConsumerRecord<String, GenericRecord> pollEvent(long timeoutMillis) throws InterruptedException {
        return records.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the Kafka consumer if it is running.
     */
    public void stopConsumer() {
        if (container != null) {
            container.stop();
        }
    }

    private Map<String, Object> getConsumerProperties(String schemaRegistryUrl) {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true",
                ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10",
                ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                "schema.registry.url", schemaRegistryUrl
        );
    }


    /**
     * Deserializes a GenericRecord into a specific Avro Event.
     *
     * @param genericRecord the GenericRecord to deserialize
     * @param clazz         the class of the specific Avro type
     * @param <T>           the type of the specific Avro type
     * @return the deserialized specific Avro type
     * @throws Exception if an error occurs during deserialization
     */
    public static <T> T deserializeGenericRecord(GenericRecord genericRecord, Class<T> clazz) throws Exception {
        SpecificDatumReader<T> datumReader = new SpecificDatumReader<>(clazz);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(genericRecord.getSchema());
        writer.write(genericRecord, encoder);
        encoder.flush();
        outputStream.close();

        byte[] valueBytes = outputStream.toByteArray();
        Decoder decoder = DecoderFactory.get().binaryDecoder(valueBytes, null);
        return datumReader.read(null, decoder);
    }
}
