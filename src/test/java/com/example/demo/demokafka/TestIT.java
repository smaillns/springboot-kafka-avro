package com.example.demo.demokafka;

import com.example.demo.demokafka.config.Event;
import com.example.demo.demokafka.config.KafkaTestConfig;
import com.example.demo.demokafka.config.KafkaTestUtils;
import com.example.demo.demokafka.event.MyEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@DirtiesContext
@SpringBootTest
@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://localhost:9092"},
        partitions = 1,
        controlledShutdown = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWireMock(port=0)
@ContextConfiguration(classes = {KafkaTestConfig.class})
public class TestIT {

    @Autowired
    KafkaTemplate<String, MyEvent> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.my-consumer.topic.main}")
    private String myMainTopic;

    @Value("${app.kafka.my-consumer.topic.retry}")
    private String myRetryTopic;

    @Value("${app.kafka.my-consumer.topic.error}")
    private String myDltTopic;

    @Value("${app.kafka.my-consumer.schema-registry.url}")
    private String schemaRegistryUrl;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private KafkaTestUtils kafkaTestUtils;

    @BeforeEach
    void setUp() {
        kafkaTestUtils = new KafkaTestUtils(embeddedKafkaBroker);

        WireMock.reset();
        WireMock.stubFor(
                WireMock.post(WireMock.urlMatching("/subjects/.*"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"id\":1}"))
        );
    }

    @AfterEach
    void tearDown() {
        kafkaTestUtils.stopConsumer();
    }

    @Test
    void test_event_flow_to_retry_and_dlt_topics() throws Exception {

        kafkaTestUtils.registerSchema(1, myMainTopic, MyEvent.getClassSchema().toString());

        // Send Event
        File EVENT_JSON = Paths.get("src", "test", "resources", "events", "event.json").toFile();
        MyEvent sentEvent = objectMapper.readValue(EVENT_JSON, MyEvent.class);
        kafkaTemplate.send(myMainTopic, "1", sentEvent);


//        await().atMost(1, TimeUnit.MINUTES).until(() -> {
//            kafkaTestUtils.setupConsumer(myRetryTopic);
//            ConsumerRecord<String, String> retryRecord = kafkaTestUtils.pollEvent(5000);
//            return retryRecord != null;
//        });

//        System.out.println("done)");
        // Validate Event in Retry Topic
        kafkaTestUtils.setupConsumer(myRetryTopic, schemaRegistryUrl);
        ConsumerRecord<String, String> retryRecord = kafkaTestUtils.pollEvent(5000);
        assertNotNull(retryRecord, "Expected an event in retry topic but none was found");
//        MyEvent retryEvent = objectMapper.readValue(retryRecord.value(), MyEvent.class);
        // Deserialize the GenericRecord into MyEvent

        try {
            byte[] valueBytes = retryRecord.value().getBytes(StandardCharsets.UTF_8); // Convert String to byte[]
            Decoder decoder = DecoderFactory.get().binaryDecoder(valueBytes, null);

            // Create a DatumReader with the appropriate schema
            DatumReader<GenericRecord> reader = new SpecificDatumReader<>(MyEvent.getClassSchema());
            GenericRecord record = reader.read(null, decoder);

            // Process the deserialized GenericRecord
            System.out.println("Decoded record: " + record);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            DatumReader<MyEvent> reader = new SpecificDatumReader<>(MyEvent.class);
//            Decoder decoder = DecoderFactory.get().binaryDecoder(retryRecord.value().getBytes(), null);
//            retryEvent = reader.read(null, decoder);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        assertEquals(sentEvent.getId(), retryEvent.getId());x

//        // Validate Event in DLT Topic
//        kafkaTestUtils.setupConsumer(myDltTopic);
//        ConsumerRecord<String, String> dltRecord = kafkaTestUtils.pollEvent(60000);
//        assertNotNull(dltRecord, "Expected an event in DLT topic but none was found");
//        MyEvent dltEvent = objectMapper.readValue(dltRecord.value(), MyEvent.class);
//        assertEquals(sentEvent.getId(), dltEvent.getId());
    }

}