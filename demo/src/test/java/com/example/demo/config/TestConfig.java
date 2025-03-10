package com.example.demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import com.mongodb.client.MongoClients;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean
    public MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer("mongo:6.0");
        container.start();
        return container;
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoDBContainer mongoDBContainer) {
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(MongoClients.create(connectionString), "test"));
    }
} 