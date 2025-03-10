package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Import;
import com.example.demo.config.TestConfig;

@SpringBootTest(classes = {ApiApplication.class, AsyncConfig.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestConfig.class)
class AsyncConfigTest {

    @Autowired
    private AsyncConfig asyncConfig;

    @Test
    void asyncExecutor_ShouldCreateThreadPoolTaskExecutor() {
        // When
        Executor executor = asyncConfig.asyncExecutor();

        // Then
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        
        assertEquals(3, taskExecutor.getCorePoolSize());
        assertEquals(3, taskExecutor.getMaxPoolSize());
        assertEquals(100, taskExecutor.getQueueCapacity());
        assertTrue(taskExecutor.getThreadNamePrefix().startsWith("AsyncThread-"));
    }

    @Test
    void asyncExecutor_ShouldBeConfiguredCorrectly() {
        // When
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.asyncExecutor();

        // Then
        assertEquals("AsyncThread-", executor.getThreadNamePrefix());
        assertTrue(executor.getThreadGroup() != null);
    }
} 