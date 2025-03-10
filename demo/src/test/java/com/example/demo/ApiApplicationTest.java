package com.example.demo;

import com.example.demo.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = ApiApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestConfig.class)
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