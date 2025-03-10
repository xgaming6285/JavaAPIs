package com.example.demo.repository.mongo;

import com.example.demo.model.mongo.AnalyticsData;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;

public interface AnalyticsDataRepository extends MongoRepository<AnalyticsData, String> {
    List<AnalyticsData> findByEventType(String eventType);
    List<AnalyticsData> findByUserId(String userId);
    List<AnalyticsData> findByTimestampBetween(Instant start, Instant end);
    List<AnalyticsData> findByEventTypeAndTimestampBetween(String eventType, Instant start, Instant end);
} 