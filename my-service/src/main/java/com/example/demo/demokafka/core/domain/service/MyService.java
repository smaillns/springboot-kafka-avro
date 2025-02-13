package com.example.demo.demokafka.core.domain.service;

import com.example.demo.demokafka.core.domain.model.MyModel;

public interface MyService {
    void handleReceivedEvent(MyModel model);
}
