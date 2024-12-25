package com.example.demo.demokafka;

import com.example.demo.demokafka.config.KafkaTestConfig;
import com.example.demo.demokafka.config.KafkaTestUtils;
import com.example.demo.demokafka.event.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@DirtiesContext
@SpringBootTest
@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://localhost:9092"},
        partitions = 1,
        controlledShutdown = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {KafkaTestConfig.class})
public class TestIT {

    @Autowired
    KafkaTemplate<String, Event> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.my-consumer.topic.main}")
    private String myMainTopic;

    @Value("${app.kafka.my-consumer.topic.retry}")
    private String myRetryTopic;

    @Value("${app.kafka.my-consumer.topic.error}")
    private String myDltTopic;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private KafkaTestUtils kafkaTestUtils;

    @BeforeEach
    void setUp() {
        kafkaTestUtils = new KafkaTestUtils(embeddedKafkaBroker);
    }

    @AfterEach
    void tearDown() {
        kafkaTestUtils.stopConsumer();
    }

    @Test
    void test_event_flow_to_retry_and_dlt_topics() throws Exception {
        // Send Event
        File EVENT_JSON = Paths.get("src", "test", "resources", "events", "event.json").toFile();
        Event sentEvent = objectMapper.readValue(EVENT_JSON, Event.class);
        kafkaTemplate.send(myMainTopic, "1", sentEvent);

        // Validate Event in Retry Topic
        kafkaTestUtils.setupConsumer(myRetryTopic);
        ConsumerRecord<String, String> retryRecord = kafkaTestUtils.pollEvent(5000);
        assertNotNull(retryRecord, "Expected an event in retry topic but none was found");
        Event retryEvent = objectMapper.readValue(retryRecord.value(), Event.class);
        assertEquals(sentEvent.getId(), retryEvent.getId());

        // Validate Event in DLT Topic
        kafkaTestUtils.setupConsumer(myDltTopic);
        ConsumerRecord<String, String> dltRecord = kafkaTestUtils.pollEvent(60000);
        assertNotNull(dltRecord, "Expected an event in DLT topic but none was found");
        Event dltEvent = objectMapper.readValue(dltRecord.value(), Event.class);
        assertEquals(sentEvent.getId(), dltEvent.getId());
    }

}