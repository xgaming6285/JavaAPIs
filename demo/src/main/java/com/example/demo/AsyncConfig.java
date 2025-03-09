package com.example.demo;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class for asynchronous execution settings.
 * Provides custom thread pool configuration for handling asynchronous operations.
 * 
 * The configuration creates a thread pool with:
 * - Core pool size: 3 threads
 * - Max pool size: 3 threads
 * - Queue capacity: 100 tasks
 * - Thread name prefix: "AsyncThread-"
 */
@Configuration
@EnableAsync
public class AsyncConfig {
  
  /**
   * Creates and configures a thread pool executor for asynchronous operations.
   * This executor is used by Spring's @Async annotation to handle asynchronous method calls.
   *
   * @return Configured ThreadPoolTaskExecutor instance
   */
  @Bean(name = "asyncExecutor")
  public Executor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(3);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("AsyncThread-");
    executor.setThreadGroup(new ThreadGroup("AsyncGroup"));
    executor.initialize();
    return executor;
  }
}
