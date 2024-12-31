package com.example.demo.demokafka;

import com.example.demo.demokafka.config.KafkaTestConfig;
import com.example.demo.demokafka.event.MyEvent;
import com.example.demo.demokafka.utils.KafkaTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.avro.generic.GenericRecord;
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
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;



@ExtendWith(MockitoExtension.class)
@DirtiesContext
@SpringBootTest
@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://localhost:9092"},
        partitions = 1,
        controlledShutdown = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWireMock(port = 0)
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
    void setUp()  {
        kafkaTestUtils = new KafkaTestUtils(embeddedKafkaBroker);

        WireMock.reset();
    }

    @AfterEach
    void tearDown() {
        kafkaTestUtils.stopConsumer();
    }

    @Test
    void test_event_flow_to_retry_and_dlt_topics() throws Exception {

        kafkaTestUtils.registerSchema(1, myMainTopic, MyEvent.getClassSchema().toString());
        kafkaTestUtils.registerSchema(1, myRetryTopic, MyEvent.getClassSchema().toString());
        kafkaTestUtils.registerSchema(1, myDltTopic, MyEvent.getClassSchema().toString());

        // Send Event
        File EVENT_JSON = Paths.get("src", "test", "resources", "events", "invalid-event.json").toFile();
        MyEvent sentEvent = objectMapper.readValue(EVENT_JSON, MyEvent.class);
        kafkaTemplate.send(myMainTopic, "1", sentEvent);


        // Validate Event in Retry Topic
        AtomicReference<ConsumerRecord<String, GenericRecord>> retryRecord = new AtomicReference<>();
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            kafkaTestUtils.setupConsumer(myRetryTopic, schemaRegistryUrl);
            retryRecord.set(kafkaTestUtils.pollEvent(1000));
            assertNotNull(retryRecord.get(), "Expected an event in retry topic but none was found");
        });
        MyEvent myEvent = KafkaTestUtils.deserializeGenericRecord(retryRecord.get().value(), MyEvent.class); // Deserialize the GenericRecord into MyEvent
        System.out.println("Mapped MyEvent: " + myEvent);
        assertEquals(sentEvent.getId(), myEvent.getId());

        // Validate Event in DLT Topic
        AtomicReference<ConsumerRecord<String, GenericRecord>> dltRecord = new AtomicReference<>();
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            kafkaTestUtils.setupConsumer(myDltTopic, schemaRegistryUrl);
            dltRecord.set(kafkaTestUtils.pollEvent(1000));
            assertNotNull(dltRecord.get(), "Expected an event in DLT topic but none was found");
        });
        MyEvent dltEvent = KafkaTestUtils.deserializeGenericRecord(retryRecord.get().value(), MyEvent.class); // Deserialize the GenericRecord into MyEvent
        System.out.println("Mapped MyEvent: " + dltEvent);
        assertEquals(sentEvent.getId(), dltEvent.getId());
    }

}