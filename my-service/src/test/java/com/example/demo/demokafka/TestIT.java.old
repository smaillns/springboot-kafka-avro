package com.example.demo.demokafka;

import com.example.demo.demokafka.config.KafkaTestConfig;
import com.example.demo.demokafka.config.PostgresTestConfig;
import com.example.demo.demokafka.core.adapter.db.entity.MyEntity;
import com.example.demo.demokafka.core.adapter.db.repository.MyRepository;
import com.example.demo.demokafka.event.MyEvent;
import com.example.demo.demokafka.utils.KafkaTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
@DirtiesContext
@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://localhost:9092"},
        partitions = 1,
        controlledShutdown = true)
@AutoConfigureWireMock(port = 0)
@ContextConfiguration(classes = {KafkaTestConfig.class, PostgresTestConfig.class})
@Testcontainers
@SpringBootTest
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

    @Value("${app.kafka.my-consumer.topic.output}")
    private String myOutputTopic;

    @Value("${app.kafka.my-consumer.schema-registry.url}")
    private String schemaRegistryUrl;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private MyRepository myRepository;

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
    void testEventIsConsumedAndSavedToDatabase() throws Exception {
        kafkaTestUtils.registerSchema(1, myMainTopic, MyEvent.getClassSchema().toString());
        kafkaTestUtils.registerSchema(1, myRetryTopic, MyEvent.getClassSchema().toString());
        kafkaTestUtils.registerSchema(1, myDltTopic, MyEvent.getClassSchema().toString());
        kafkaTestUtils.registerSchema(1, myOutputTopic, MyEvent.getClassSchema().toString());

        // Send Event
        File EVENT_JSON = Paths.get("src", "test", "resources", "events", "event.json").toFile();
        MyEvent sentEvent = objectMapper.readValue(EVENT_JSON, MyEvent.class);
        kafkaTemplate.send(myMainTopic, "1", sentEvent);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<MyEntity> savedEntity = myRepository.findById(sentEvent.getId());
            savedEntity.ifPresent(myEntity -> assertEquals(sentEvent.getLabel(), myEntity.toModel().getLabel(), "The saved item should have the same id as the sent item"));
        });

        // Check Event in the Output-topic
        AtomicReference<ConsumerRecord<String, GenericRecord>> outputRecord = new AtomicReference<>();
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            kafkaTestUtils.setupConsumer(myOutputTopic, schemaRegistryUrl);
            outputRecord.set(kafkaTestUtils.pollEvent(1000));
            assertNotNull(outputRecord.get(), "Expected an event in the output topic but none was found");
        });
        MyEvent myEvent = KafkaTestUtils.deserializeGenericRecord(outputRecord.get().value(), MyEvent.class); // Deserialize the GenericRecord into MyEvent
        assertEquals(sentEvent.getId(), myEvent.getId());
    }

    @Test
    void testEventFlowToRetryTopic() throws Exception {

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
        MyEvent myEvent = KafkaTestUtils.deserializeGenericRecord(retryRecord.get().value(), MyEvent.class);
        System.out.println("Mapped MyEvent: " + myEvent);
        assertEquals(sentEvent.getId(), myEvent.getId());
    }

}