package com.example.demo.demokafka.core.domain.service;


import com.example.demo.demokafka.common.exceptions.FunctionalException;
import com.example.demo.demokafka.core.adapter.messaging.KafkaMyEventProducer;
import com.example.demo.demokafka.core.domain.model.MyModel;

import com.example.demo.demokafka.core.port.ExternalApiClient;
import com.example.demo.demokafka.core.port.MyEventDataGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Slf4j
@Service
@RequiredArgsConstructor
public class MyServiceImpl implements MyService {

    private final MyEventDataGateway myEventDataGateway;
    private final KafkaMyEventProducer publisher;
    private final ExternalApiClient externalApiClient;


    @Override
    public void handleReceivedEvent(MyModel model) {
        if(!model.isValid()) {
            throw new FunctionalException("Invalid model");
        }
        log.info("Handling model: " + model);
        myEventDataGateway.saveEvent(model);
        publisher.publishOutputEvent(model);

        // Make API call to jsonplaceholder
        String response = externalApiClient.postToExternalApi(model);
        log.info("Response from external API: " + response);
    }
}
