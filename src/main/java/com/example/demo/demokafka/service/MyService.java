package com.example.demo.demokafka.service;

import com.example.demo.demokafka.model.MyModel;

public interface MyService {
    void handleReceivedEvent(MyModel model);
}
