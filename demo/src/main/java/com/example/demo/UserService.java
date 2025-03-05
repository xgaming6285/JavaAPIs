package com.example.demo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong();
    private AtomicInteger failureCounter = new AtomicInteger(0);
    
    // Get the list of all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Cache a user by id. Cache key is the id.
    @Cacheable(value = "user", key = "#id")
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }
    
    // Get users with pagination
    public Page<User> getUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    // Update password: check old password before updating
    public User updatePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("Old password does not match");
        }
        user.setPassword(newPassword);
        return userRepository.save(user);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Optional<User>> getUserByIdAsync(Long id) {
        Optional<User> user = userRepository.findById(id);
        return CompletableFuture.completedFuture(user);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserById")
    public User getUserByIdWithCircuitBreaker(Long id) {
        // Simulate failure every other request
        if (failureCounter.getAndIncrement() % 2 == 0) {
            throw new RuntimeException("Simulated service failure");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    // Fallback method in case of failure
    public User fallbackGetUserById(Long id, Throwable t) {
        return new User("fallback-user", "fallback@example.com", "fallback-password");
    }
}
