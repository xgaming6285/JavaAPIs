package com.example.demo.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    
    /**
     * Creates a timed aspect for measuring method execution times.
     * This enables the @Timed annotation on methods.
     *
     * @param registry the meter registry
     * @return the configured timed aspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    /**
     * Creates a JVM garbage collection metrics collector.
     *
     * @return the configured GC metrics collector
     */
    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }
    
    /**
     * Creates a JVM memory metrics collector.
     *
     * @return the configured memory metrics collector
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }
    
    /**
     * Creates a JVM thread metrics collector.
     *
     * @return the configured thread metrics collector
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }
    
    /**
     * Creates a processor metrics collector.
     *
     * @return the configured processor metrics collector
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }
    
    /**
     * Creates an uptime metrics collector.
     *
     * @return the configured uptime metrics collector
     */
    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }
} 