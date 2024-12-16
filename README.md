# springboot-kafka-avro


Thi setup inclides only the essential properties required to connect and interact with a kafka broker.


```
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: group-id
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```


This configuration includes:  
- bootstrap-servers: The address of the Kafka broker.
- consumer properties: group-id, key-deserializer, and value-deserializer.
- producer properties: key-serializer and value-serializer.

This setup is sufficient to start consuming and producing messages with Kafka.