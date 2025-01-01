package com.example.demo.demokafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyModel {

    private Long id;
    private String label;

    public boolean isValid() {
        return id != 0;
    }
}

