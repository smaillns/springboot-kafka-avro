package com.example.demo.demokafka.service;


import com.example.demo.demokafka.exceptions.FunctionalException;
import com.example.demo.demokafka.model.MyModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class MyServiceImpl implements MyService {

    @Override
    public void handleReceivedEvent(MyModel model) {
        if(!model.isValid()) {
            throw new FunctionalException("Invalid model");
        }
        log.info("Handling model: " + model);
    }
}
