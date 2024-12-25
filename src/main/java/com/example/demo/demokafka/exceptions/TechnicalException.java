package com.example.demo.demokafka.exceptions;

import lombok.Data;

@Data
public class TechnicalException {
    private String message;
    private String error_description;
}
