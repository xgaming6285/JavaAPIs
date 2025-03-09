package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MeterRegistry meterRegistry;

    private UserController userController;
    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        
        testUser = new User("testUser", "test@example.com", "password");
        testUser.setId(1L);
        testUser.setRoles(Set.of("ROLE_USER"));
        testUser.setActive(true);

        testUserDTO = new UserDTO(testUser.getId(), testUser.getUsername(), testUser.getEmail());
        
        userController = new UserController(userService, meterRegistry);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(testUser));

        List<UserDTO> result = userController.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDTO.getUsername(), result.get(0).getUsername());
        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<UserDTO> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(testUserDTO.getUsername(), responseBody.getUsername());
        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldThrowException() {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userController.getUserById(1L));
        verify(userService).getUserById(1L);
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testUser");
        createUserDTO.setEmail("test@example.com");
        createUserDTO.setPassword("password");

        when(userService.createUser(any(User.class))).thenReturn(testUser);

        ResponseEntity<UserDTO> response = userController.createUser(createUserDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserDTO responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(testUserDTO.getUsername(), responseBody.getUsername());
        verify(userService).createUser(any(User.class));
        assertEquals(1.0, meterRegistry.counter("api.user.creation").count());
    }

    @Test
    void updateUser_WhenUserExists_ShouldReturnUpdatedUser() {
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(testUser);

        ResponseEntity<UserDTO> response = userController.updateUser(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(testUserDTO.getUsername(), responseBody.getUsername());
        verify(userService).updateUser(eq(1L), any(User.class));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser(1L);
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        when(userService.searchUsersByUsername("test"))
            .thenReturn(Collections.singletonList(testUser));

        List<UserDTO> result = userController.searchUsers("test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDTO.getUsername(), result.get(0).getUsername());
        verify(userService).searchUsersByUsername("test");
    }

    @Test
    void getUsersPaginated_ShouldReturnPageOfUsers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser));
        
        when(userService.getUsersPaginated(pageRequest)).thenReturn(userPage);

        Page<UserDTO> result = userController.getUsersPaginated(pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testUserDTO.getUsername(), result.getContent().get(0).getUsername());
        verify(userService).getUsersPaginated(pageRequest);
    }

    @Test
    void updatePassword_WhenSuccessful_ShouldReturnUpdatedUser() {
        PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
        passwordUpdateDTO.setOldPassword("oldPassword");
        passwordUpdateDTO.setNewPassword("newPassword");

        when(userService.updatePassword(eq(1L), anyString(), anyString())).thenReturn(testUser);

        ResponseEntity<UserDTO> response = userController.updatePassword(1L, passwordUpdateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(testUserDTO.getUsername(), responseBody.getUsername());
        verify(userService).updatePassword(eq(1L), anyString(), anyString());
    }

    @Test
    void getUserWithCircuitBreaker_WhenSuccessful_ShouldReturnUser() {
        when(userService.getUserByIdWithCircuitBreaker(1L)).thenReturn(testUser);

        ResponseEntity<UserDTO> response = userController.getUserWithCircuitBreaker(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(testUserDTO.getUsername(), responseBody.getUsername());
        verify(userService).getUserByIdWithCircuitBreaker(1L);
    }

    @Test
    void getActiveUsers_ShouldReturnActiveUsers() {
        when(userService.getActiveUsers()).thenReturn(Collections.singletonList(testUser));

        ResponseEntity<List<UserDTO>> response = userController.getActiveUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserDTO> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(1, responseBody.size());
        assertEquals(testUserDTO.getUsername(), responseBody.get(0).getUsername());
        verify(userService).getActiveUsers();
    }

    @Test
    void getInactiveUsers_ShouldReturnInactiveUsers() {
        when(userService.getInactiveUsers()).thenReturn(Collections.singletonList(testUser));

        ResponseEntity<List<UserDTO>> response = userController.getInactiveUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserDTO> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(1, responseBody.size());
        assertEquals(testUserDTO.getUsername(), responseBody.get(0).getUsername());
        verify(userService).getInactiveUsers();
    }

    @Test
    void getUsersByEmailDomain_ShouldReturnUsersWithMatchingDomain() {
        when(userService.getUsersByEmailDomain("example.com"))
            .thenReturn(Collections.singletonList(testUser));

        ResponseEntity<List<UserDTO>> response = userController.getUsersByEmailDomain("example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserDTO> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(1, responseBody.size());
        assertEquals(testUserDTO.getUsername(), responseBody.get(0).getUsername());
        verify(userService).getUsersByEmailDomain("example.com");
    }

    @Test
    void getUsersByRole_ShouldReturnUsersWithMatchingRole() {
        when(userService.getUsersByRole("ROLE_USER"))
            .thenReturn(Collections.singletonList(testUser));

        ResponseEntity<List<UserDTO>> response = userController.getUsersByRole("ROLE_USER");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserDTO> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(1, responseBody.size());
        assertEquals(testUserDTO.getUsername(), responseBody.get(0).getUsername());
        verify(userService).getUsersByRole("ROLE_USER");
    }

    @Test
    void getUsersByMinimumRoles_ShouldReturnUsersWithMinimumRoles() {
        when(userService.getUsersByMinimumRoles(1))
            .thenReturn(Collections.singletonList(testUser));

        ResponseEntity<List<UserDTO>> response = userController.getUsersByMinimumRoles(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserDTO> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(1, responseBody.size());
        assertEquals(testUserDTO.getUsername(), responseBody.get(0).getUsername());
        verify(userService).getUsersByMinimumRoles(1);
    }

    @Test
    void updateUserRoles_WhenSuccessful_ShouldReturnUpdatedUser() {
        UpdateRolesDTO rolesDTO = new UpdateRolesDTO();
        rolesDTO.setRoles(Set.of("ROLE_ADMIN"));

        when(userService.updateUserRoles(eq(1L), any())).thenReturn(testUser);

        ResponseEntity<UserDTO> response = userController.updateUserRoles(1L, rolesDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals(testUserDTO.getUsername(), responseBody.getUsername());
        verify(userService).updateUserRoles(eq(1L), any());
    }
} 