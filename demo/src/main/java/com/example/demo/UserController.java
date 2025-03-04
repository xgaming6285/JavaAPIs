import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // GET all users
    @GetMapping("")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers()
                          .stream()
                          .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail()))
                          .collect(Collectors.toList());
    }
    
    // GET user by id
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id);
        return userOpt.map(user -> ResponseEntity.ok(
                                    new UserDTO(user.getId(), user.getUsername(), user.getEmail())))
                      .orElse(ResponseEntity.notFound().build());
    }
    
    // POST create a new user
    @PostMapping("")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserDTO createUserDTO) {
        // Convert CreateUserDTO to User entity
        User user = new User(createUserDTO.getUsername(), createUserDTO.getEmail(), createUserDTO.getPassword());
        User created = userService.createUser(user);
        UserDTO userDTO = new UserDTO(created.getId(), created.getUsername(), created.getEmail());
        return ResponseEntity.status(201).body(userDTO);
    }
    
    // PUT update an existing user
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        UserDTO userDTO = new UserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail());
        return ResponseEntity.ok(userDTO);
    }
    
    // DELETE a user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    // GET search users by username (partial match)
    @GetMapping("/search")
    public List<UserDTO> searchUsers(@RequestParam("username") String username) {
        return userService.searchUsersByUsername(username)
                          .stream()
                          .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail()))
                          .collect(Collectors.toList());
    }
    
    // GET users with pagination and sorting
    @GetMapping("/paginated")
    public Page<UserDTO> getUsersPaginated(Pageable pageable) {
        Page<User> usersPage = userService.getUsersPaginated(pageable);
        return usersPage.map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail()));
    }
    
    // PUT update password endpoint
    @PutMapping("/{id}/password")
    public ResponseEntity<UserDTO> updatePassword(@PathVariable Long id,
                                                  @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        User updatedUser = userService.updatePassword(id,
                                passwordUpdateDTO.getOldPassword(),
                                passwordUpdateDTO.getNewPassword());
        UserDTO userDTO = new UserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail());
        return ResponseEntity.ok(userDTO);
    }
}
