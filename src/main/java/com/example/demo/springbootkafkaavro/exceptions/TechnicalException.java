package com.example.demo.springbootkafkaavro.exceptions;

import lombok.Data;

@Data
public class TechnicalException {
    private String message;
    private String error_description;
}
