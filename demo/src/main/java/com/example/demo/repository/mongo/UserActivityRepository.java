package com.example.demo.repository.mongo;

import com.example.demo.model.mongo.UserActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;

public interface UserActivityRepository extends MongoRepository<UserActivity, String> {
    List<UserActivity> findByUserIdOrderByTimestampDesc(String userId);
    List<UserActivity> findByTimestampBetween(Instant start, Instant end);
    List<UserActivity> findByActionAndTimestampBetween(String action, Instant start, Instant end);
} 