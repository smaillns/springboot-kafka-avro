package com.example.demo.demokafka.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@TestConfiguration
public class PostgresTestConfig {

//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");
//
//    static {
//        postgres.start();
//        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
//        System.setProperty("spring.datasource.username", postgres.getUsername());
//        System.setProperty("spring.datasource.password", postgres.getPassword());
//    }
//
//    @Bean
//    public DataSource dataSource() {
//        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setJdbcUrl(postgres.getJdbcUrl());
//        dataSource.setUsername(postgres.getUsername());
//        dataSource.setPassword(postgres.getPassword());
//        return dataSource;
//    }
}
