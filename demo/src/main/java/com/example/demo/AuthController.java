import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    // Registration endpoint
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody CreateUserDTO createUserDTO) {
        User user = new User(createUserDTO.getUsername(), createUserDTO.getEmail(), createUserDTO.getPassword());
        User newUser = userService.createUser(user);
        UserDTO userDTO = new UserDTO(newUser.getId(), newUser.getUsername(), newUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }
    
    // Login endpoint (simplified)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        return userService.getUserByUsername(loginRequest.getUsername())
            .filter(user -> user.getPassword().equals(loginRequest.getPassword()))
            .map(user -> ResponseEntity.ok("Login successful"))
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                  .body("Invalid credentials"));
    }
}
