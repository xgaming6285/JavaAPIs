package com.example.demo.repository.mongo;

import com.example.demo.model.mongo.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByUserId(String userId);
    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, String resourceId);
    List<AuditLog> findByTimestampBetween(Instant start, Instant end);
    List<AuditLog> findByActionAndTimestampBetween(String action, Instant start, Instant end);
    List<AuditLog> findByUserIdAndTimestampBetween(String userId, Instant start, Instant end);
} 