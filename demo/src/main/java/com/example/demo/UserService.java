package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.HashSet;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String USER_NOT_FOUND = "User not found";

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final AtomicInteger failureCounter = new AtomicInteger(0);
    private final Map<Long, User> userCache = new ConcurrentHashMap<>();
    private final Map<Long, String> verificationTokenCache = new ConcurrentHashMap<>();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Cacheable(value = "user", key = "#id")
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        logger.info("Creating user with username: {}", user.getUsername());
        
        // Initialize with ROLE_USER if roles are empty
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Set.of("ROLE_USER"));
        }
        
        if (!isPasswordHashed(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        try {
            User savedUser = userRepository.save(user);
            userCache.put(savedUser.getId(), savedUser);
            return savedUser;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        updateUserFields(user, userDetails);
        User updatedUser = userRepository.save(user);
        userCache.put(updatedUser.getId(), updatedUser);
        return updatedUser;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        userCache.remove(id);
        verificationTokenCache.remove(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return Optional.ofNullable(username)
            .flatMap(name -> userCache.values().stream()
                .filter(user -> name.equals(user.getUsername()))
                .findFirst()
                .or(() -> userRepository.findByUsername(name)));
    }

    public List<User> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public Page<User> getUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User updatePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        userCache.put(updatedUser.getId(), updatedUser);
        return updatedUser;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Optional<User>> getUserByIdAsync(Long id) {
        return CompletableFuture.completedFuture(getUserById(id));
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserById")
    public User getUserByIdWithCircuitBreaker(Long id) {
        if (failureCounter.getAndIncrement() % 2 == 0) {
            throw new RuntimeException("Simulated service failure");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    }

    public User fallbackGetUserById(Long id, Throwable t) {
        logger.warn("Circuit breaker fallback for user ID: {}", id);
        return new User("fallback-user", "fallback@example.com", "fallback-password");
    }

    public boolean verifyUserCredentials(String username, String password) {
        return getUserByUsername(username)
            .map(user -> passwordEncoder.matches(password, user.getPassword()))
            .orElse(false);
    }

    public Optional<User> getUserByToken(String token) {
        return userRepository.findByToken(token)
            .or(() -> findUserByTokenInCache(token));
    }

    public void saveResetToken(Long userId, String token) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        user.setToken(token);
        userRepository.save(user);
    }

    public void saveVerificationToken(Long userId, String token) {
        verificationTokenCache.put(userId, token);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private boolean isPasswordHashed(String password) {
        return password != null && password.startsWith("$2a$");
    }

    private void updateUserFields(User user, User userDetails) {
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        
        if (userDetails.getPassword() != null && 
            !userDetails.getPassword().isEmpty() && 
            !isPasswordHashed(userDetails.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
    }

    private Optional<User> findUserByTokenInCache(String token) {
        return verificationTokenCache.entrySet().stream()
            .filter(entry -> token.equals(entry.getValue()))
            .map(entry -> userRepository.findById(entry.getKey()).orElse(null))
            .findFirst();
    }

    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    public List<User> getInactiveUsers() {
        return userRepository.findByActiveFalse();
    }

    public List<User> getUsersByEmailDomain(String domain) {
        return userRepository.findByEmailDomain(domain);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRolesContaining(role);
    }

    public List<User> getUsersByMinimumRoles(int minRoles) {
        return userRepository.findByMinimumRoles(minRoles);
    }

    public List<User> searchUsers(String username, String email, Boolean active, String role) {
        return userRepository.findByMultipleCriteria(username, email, active, role);
    }

    public User updateUserRoles(Long id, Set<String> roles) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.getRoles().clear();  
        user.setRoles(new HashSet<>(roles));  
        
        try {
            User updatedUser = userRepository.save(user);
            userCache.put(updatedUser.getId(), updatedUser);
            return updatedUser;
        } catch (Exception e) {
            logger.error("Error updating user roles: {}", e.getMessage());
            throw new RuntimeException("Failed to update user roles", e);
        }
    }
}
