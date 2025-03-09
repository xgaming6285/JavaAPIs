package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = ApiApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ApiApplicationTest {

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully
    }

    @Test
    void mainMethodStartsApplication() {
        // Test the main method can be called without throwing exceptions
        ApiApplication.main(new String[]{});
    }
} 