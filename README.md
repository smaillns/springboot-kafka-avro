
## Kafka Avro Serialization with Schema Registry 


## About

> This project demonstrates the usage of Kafka Schema Registry with Avro serialization in a Spring Boot application. 
>
> The setup includes Kafka configuration used in the [eyes-flows](https://github.com/dktunited/eyes-visibility) project, essential properties required to connect and interact with a Kafka broker.

> [!NOTE]
> The purpose is to focus on the Kafka configuration and make a POC of integration tests using two methods:
> 1. Using the `EmbeddedKafka` provided by Spring in the `spring-kafka-test` dependency.
> 2. Setting up a Testcontainers ecosystem with Kafka, Zookeeper, and Confluent Schema Registry.


## Project Structure

The project is modular, consisting of:
- `avro-schema`: A module for Avro schemas generation and management.
- `my-service`: The main service module that includes the Spring Boot application, the kafka configuration and the integrationtests

the directory structure of the project is as follows
```
project/
├── avro-schema/
│   ├── src/
│   │   ├── main/
│   │   │   ├── avro/
│   │   │   └── resources/
│   └── pom.xml
├── my-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   ├── test/
│   │   │   ├── java/
│   │   │   └── resources/
│   └── pom.xml
├── scripts/
│   ├── docker/
│   │   └── docker-compose.yml
│   └── akhq/
│       ├── akhq-config.yml
│       └── akhq.jar
├── pom.xml
└── README.md
```


## Prerequisites

- Docker and Docker Compose
- Java 23
- Maven

## Setup

### Docker Compose

To launch Kafka and Schema Registry locally, use the provided `docker-compose.yml` file.

```sh
docker-compose up -d
```

## AKHQ
To launch AKHQ, add a new IntelliJ IDEA configuration with the following shell script:
```sh
java -Dmicronaut.config.files=scripts/akhq/akhq-config.yml -jar scripts/akhq/akhq.jar
```


## Configuration
### Kafka Configuration
The current configuration uses a retry topic and a DLT (Dead Letter Topic). The number of retries, delay, and other properties are configured in the application configuration files.

**Example Configuration**

```yaml
app:
  kafka:
    my-consumer:
      topic:
        main: my-main-topic
        retry: my-retry-topic
        error: my-dlt-topic
      client-id: my-client-id
      group-id: my-group-id
      enabled: true
    schema-registry:
      url: http://localhost:8081
```


## Integration Test
### Using EmbeddedKafka
The `EmbeddedKafka` provided by Spring in the `spring-kafka-test` dependency is used for integration tests.


### Using Testcontainers
A Testcontainers ecosystem is set up with Kafka, Zookeeper, and Confluent Schema Registry for integration tests.


### Running Tests
To run the integration tests, use the following command:
    
    ```sh   
    mvn clean test
    ```

### Kafka Mechanism
The current configuration uses a retry topic and a DLT (Dead Letter Topic). The number of retries, delay, and other properties are configured in the application configuration files. The retry mechanism ensures that messages are retried a specified number of times before being sent to the DLT.