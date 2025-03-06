package com.example.demo.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    /**
     * Creates and configures a TimedAspect bean for Micrometer metrics.
     * 
     * This method sets up a TimedAspect, which is used to add timing metrics
     * to methods annotated with @Timed. It integrates with Spring's AOP to
     * automatically time method executions and record the results in the
     * provided MeterRegistry.
     *
     * @param registry The MeterRegistry to which timing metrics will be reported
     * @return A configured TimedAspect instance
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
} 