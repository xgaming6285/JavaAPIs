package com.example.demo;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class to set up asynchronous processing in the application.
 * This class defines a custom thread pool executor for handling asynchronous tasks.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * Bean definition for the asynchronous executor.
     * 
     * @return an Executor instance configured with a thread pool.
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // Set the core number of threads
        executor.setMaxPoolSize(3); // Set the maximum allowed number of threads
        executor.setQueueCapacity(100); // Set the capacity of the queue for holding tasks before they are executed
        executor.setThreadNamePrefix("AsyncThread-"); // Set the prefix for the names of the threads
        executor.initialize(); // Initialize the executor
        return executor; // Return the configured executor
    }
}
