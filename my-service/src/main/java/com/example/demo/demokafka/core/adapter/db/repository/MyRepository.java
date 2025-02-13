package com.example.demo.demokafka.core.adapter.db.repository;

import com.example.demo.demokafka.core.adapter.db.entity.MyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyRepository extends JpaRepository<MyEntity, Long> {
}
