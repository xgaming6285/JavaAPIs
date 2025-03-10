package com.example.demo.repository.mongo;

import com.example.demo.model.mongo.UserSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends MongoRepository<UserSession, String> {
    Optional<UserSession> findBySessionToken(String sessionToken);
    List<UserSession> findByUserId(String userId);
    List<UserSession> findByUserIdAndActive(String userId, boolean active);
    List<UserSession> findByLastAccessTimeBefore(Instant time);
    Optional<UserSession> findBySessionTokenAndActive(String sessionToken, boolean active);
} 