package com.example.demo.demokafka;

import com.decathlon.tzatziki.kafka.KafkaInterceptor;
import com.decathlon.tzatziki.steps.KafkaSteps;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.util.Maps;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

import static com.decathlon.tzatziki.utils.MockFaster.url;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = DemoKafkaApplication.class)
@ContextConfiguration(initializers = DemoKafkaSteps.Initializer.class, classes = KafkaInterceptor.class)
@Slf4j
public class DemoKafkaSteps {

    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17").withTmpFs(Maps.of("/var/lib/postgresql/data", "rw"));

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            System.setProperty("EXTERNAL_URL", url());

            KafkaSteps.start(); // this will start the embedded kafka
            postgres.start(); // this will start the embedded postgres
            
            String bootstrapServers = KafkaSteps.bootstrapServers();
            String schemaRegistryUrl = KafkaSteps.schemaRegistryUrl();
            log.info("Tzatziki bootstrapServers: {}", bootstrapServers);
            log.info("Tzatziki schemaRegistryUrl: {}", schemaRegistryUrl);
            
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "spring.kafka.consumer.auto-offset-reset=earliest",
                    "spring.kafka.bootstrap-servers=" + bootstrapServers,
                    "spring.kafka.properties.schema.registry.url=" + schemaRegistryUrl,
                    "app.kafka.my-consumer.auto-offset-reset=earliest",
                    "app.kafka.my-consumer.bootstrap-servers=" + bootstrapServers,
                    "app.kafka.my-consumer.schema-registry.url=" + schemaRegistryUrl 
                        // in-memory schema registry
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
