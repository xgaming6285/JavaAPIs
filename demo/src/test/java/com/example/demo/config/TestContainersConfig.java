package com.example.demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainersConfig {
    
    @Bean(initMethod = "start", destroyMethod = "stop")
    @SuppressWarnings("resource")
    public MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
                .withExposedPorts(27017);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @SuppressWarnings("resource")
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withExposedPorts(5432);
    }
} 