package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Map<Long, User> userCache;
    private Map<Long, String> verificationTokenCache;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userCache = spy(new ConcurrentHashMap<>());
        verificationTokenCache = spy(new ConcurrentHashMap<>());
        
        testUser = new User("testUser", "test@example.com", "password");
        testUser.setId(1L);
        testUser.setRoles(Set.of("ROLE_USER"));
        testUser.setActive(true);
        
        // Inject the spied maps into the service
        userService = new UserService();
        userService.userCache = userCache;
        userService.verificationTokenCache = verificationTokenCache;
        userService.userRepository = userRepository;
        userService.passwordEncoder = passwordEncoder;
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(testUser);

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
        verify(userCache).put(eq(testUser.getId()), any(User.class));
    }

    @Test
    void updateUser_WhenUserExists_ShouldReturnUpdatedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = new User("updatedUser", "updated@example.com", "newPassword");
        User result = userService.updateUser(1L, updatedUser);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(userCache).put(eq(testUser.getId()), any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
        verify(userCache).remove(1L);
        verify(verificationTokenCache).remove(1L);
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserByUsername("testUser");

        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    void searchUsersByUsername_ShouldReturnMatchingUsers() {
        when(userRepository.findByUsernameContainingIgnoreCase("test"))
            .thenReturn(Collections.singletonList(testUser));

        List<User> result = userService.searchUsersByUsername("test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepository).findByUsernameContainingIgnoreCase("test");
    }

    @Test
    void getUsersPaginated_ShouldReturnPageOfUsers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser));
        
        when(userRepository.findAll(pageRequest)).thenReturn(userPage);

        Page<User> result = userService.getUsersPaginated(pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testUser.getUsername(), result.getContent().get(0).getUsername());
        verify(userRepository).findAll(pageRequest);
    }

    @Test
    void updatePassword_WhenValidOldPassword_ShouldUpdatePassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updatePassword(1L, "oldPassword", "newPassword");

        assertNotNull(result);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
        verify(userCache).put(eq(testUser.getId()), any(User.class));
    }

    @Test
    void updatePassword_WhenInvalidOldPassword_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, 
            () -> userService.updatePassword(1L, "wrongPassword", "newPassword"));
    }

    @Test
    void getActiveUsers_ShouldReturnActiveUsers() {
        when(userRepository.findByActiveTrue())
            .thenReturn(Collections.singletonList(testUser));

        List<User> result = userService.getActiveUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(userRepository).findByActiveTrue();
    }

    @Test
    void getInactiveUsers_ShouldReturnInactiveUsers() {
        testUser.setActive(false);
        when(userRepository.findByActiveFalse())
            .thenReturn(Collections.singletonList(testUser));

        List<User> result = userService.getInactiveUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isActive());
        verify(userRepository).findByActiveFalse();
    }

    @Test
    void getUsersByEmailDomain_ShouldReturnUsersWithMatchingDomain() {
        when(userRepository.findByEmailDomain("example.com"))
            .thenReturn(Collections.singletonList(testUser));

        List<User> result = userService.getUsersByEmailDomain("example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getEmail().endsWith("example.com"));
        verify(userRepository).findByEmailDomain("example.com");
    }

    @Test
    void getUsersByRole_ShouldReturnUsersWithMatchingRole() {
        when(userRepository.findByRolesContaining("ROLE_USER"))
            .thenReturn(Collections.singletonList(testUser));

        List<User> result = userService.getUsersByRole("ROLE_USER");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getRoles().contains("ROLE_USER"));
        verify(userRepository).findByRolesContaining("ROLE_USER");
    }

    @Test
    void updateUserRoles_WhenUserExists_ShouldUpdateRoles() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        Set<String> newRoles = Set.of("ROLE_ADMIN");
        User result = userService.updateUserRoles(1L, newRoles);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(userCache).put(eq(testUser.getId()), any(User.class));
    }

    @Test
    void updateUserRoles_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Set<String> newRoles = Set.of("ROLE_ADMIN");
        assertThrows(UserNotFoundException.class, 
            () -> userService.updateUserRoles(1L, newRoles));
    }

    @Test
    void verifyUserCredentials_WhenValidCredentials_ShouldReturnTrue() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        boolean result = userService.verifyUserCredentials("testUser", "password");

        assertTrue(result);
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    void verifyUserCredentials_WhenInvalidCredentials_ShouldReturnFalse() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        boolean result = userService.verifyUserCredentials("testUser", "wrongPassword");

        assertFalse(result);
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    void getUserByToken_WhenTokenExists_ShouldReturnUser() {
        when(userRepository.findByToken("testToken")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserByToken("testToken");

        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByToken("testToken");
    }

    @Test
    void saveResetToken_ShouldUpdateUserToken() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.saveResetToken(1L, "newToken");

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }
} 