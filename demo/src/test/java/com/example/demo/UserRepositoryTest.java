package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenFindByUsername_thenReturnUser() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .active(true)
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByUsername("testuser");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void whenFindByEmail_thenReturnUser() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .active(true)
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void whenFindByToken_thenReturnUser() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .token("test-token")
                .active(true)
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByToken("test-token");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo(user.getToken());
    }

    @Test
    void whenFindByUsernameContaining_thenReturnUsers() {
        // given
        User user1 = User.builder()
                .username("testuser1")
                .email("test1@example.com")
                .password("password123")
                .active(true)
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();
        User user2 = User.builder()
                .username("testuser2")
                .email("test2@example.com")
                .password("password123")
                .active(true)
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // when
        Page<User> found = userRepository.findByUsernameContainingIgnoreCase("testuser", Pageable.unpaged());

        // then
        assertThat(found.getTotalElements()).isEqualTo(2);
        assertThat(found.getContent()).extracting(User::getUsername)
                .containsExactlyInAnyOrder("testuser1", "testuser2");
    }

    @Test
    void whenFindByEmailDomain_thenReturnUsers() {
        // given
        User user1 = User.builder()
                .username("testuser1")
                .email("test1@example.com")
                .password("password123")
                .active(true)
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();
        User user2 = User.builder()
                .username("testuser2")
                .email("test2@different.com")
                .password("password123")
                .active(true)
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // when
        Page<User> found = userRepository.findByEmailDomain("example.com", Pageable.unpaged());

        // then
        assertThat(found.getTotalElements()).isEqualTo(1);
        assertThat(found.getContent().get(0).getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    void whenFindByMultipleCriteria_thenReturnFilteredUsers() {
        // given
        User user1 = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("password123")
                .active(true)
                .roles(new HashSet<>(Arrays.asList("ADMIN")))
                .build();
        User user2 = User.builder()
                .username("user")
                .email("user@example.com")
                .password("password123")
                .active(true)
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // when
        Page<User> found = userRepository.findByMultipleCriteria(
                "admin",
                "example.com",
                true,
                "ADMIN",
                Pageable.unpaged()
        );

        // then
        assertThat(found.getTotalElements()).isEqualTo(1);
        assertThat(found.getContent().get(0).getUsername()).isEqualTo("admin");
        assertThat(found.getContent().get(0).getRoles()).contains("ADMIN");
    }
} 