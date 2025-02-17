package com.example.demo.demokafka.core.adapter.db;

import com.example.demo.demokafka.core.adapter.db.entity.MyEntity;
import com.example.demo.demokafka.core.adapter.db.repository.MyRepository;
import com.example.demo.demokafka.core.domain.model.MyModel;
import com.example.demo.demokafka.core.port.MyEventDataGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyEventDataAdapter implements MyEventDataGateway {

    private final MyRepository myRepository;

    @Override
    public void saveEvent(MyModel event) {
        myRepository.save(MyEntity.fromModel(event));
    }
}
