package com.example.demo.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.Instant;

@Document(collection = "user_sessions")
public class UserSession {
    @Id
    private String id;
    private String userId;
    @Indexed(expireAfterSeconds = 3600) // TTL index - expires after 1 hour
    private Instant lastAccessTime;
    private String sessionToken;
    private String ipAddress;
    private String userAgent;
    private boolean active;
    private Instant createdAt;

    // Constructors
    public UserSession() {}

    public UserSession(String userId, String sessionToken, String ipAddress, String userAgent) {
        this.userId = userId;
        this.sessionToken = sessionToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.active = true;
        this.createdAt = Instant.now();
        this.lastAccessTime = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(Instant lastAccessTime) { this.lastAccessTime = lastAccessTime; }
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
} 