package com.example.demo.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;

@Document(collection = "analytics_data")
public class AnalyticsData {
    @Id
    private String id;
    private String eventType;
    private String userId;
    private Map<String, Object> metadata;
    private Instant timestamp;

    public AnalyticsData() {}

    public AnalyticsData(String eventType, String userId, Map<String, Object> metadata) {
        this.eventType = eventType;
        this.userId = userId;
        this.metadata = metadata;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
} 