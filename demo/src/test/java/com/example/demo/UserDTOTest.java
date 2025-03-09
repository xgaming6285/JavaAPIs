package com.example.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void testNoArgsConstructor() {
        UserDTO userDTO = new UserDTO();
        assertNotNull(userDTO);
        assertNull(userDTO.getId());
        assertNull(userDTO.getUsername());
        assertNull(userDTO.getEmail());
    }

    @Test
    void testAllArgsConstructor() {
        Long id = 1L;
        String username = "testUser";
        String email = "test@example.com";

        UserDTO userDTO = new UserDTO(id, username, email);

        assertEquals(id, userDTO.getId());
        assertEquals(username, userDTO.getUsername());
        assertEquals(email, userDTO.getEmail());
    }

    @Test
    void testSettersAndGetters() {
        UserDTO userDTO = new UserDTO();

        Long id = 1L;
        String username = "testUser";
        String email = "test@example.com";

        userDTO.setId(id);
        userDTO.setUsername(username);
        userDTO.setEmail(email);

        assertEquals(id, userDTO.getId());
        assertEquals(username, userDTO.getUsername());
        assertEquals(email, userDTO.getEmail());
    }
} 