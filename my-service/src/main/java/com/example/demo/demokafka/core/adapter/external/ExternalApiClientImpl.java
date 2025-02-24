package com.example.demo.demokafka.core.adapter.external;

import com.example.demo.demokafka.core.domain.model.MyModel;
import com.example.demo.demokafka.core.port.ExternalApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ExternalApiClientImpl implements ExternalApiClient {


    @Value("${app.external-api.url}")
    String externalUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String postToExternalApi(MyModel model) {
        // Make a POST request to jsonplaceholder https://jsonplaceholder.typicode.com/
        String url = externalUrl + "/posts";
        return restTemplate.postForObject(url, model, String.class);
    }
}
