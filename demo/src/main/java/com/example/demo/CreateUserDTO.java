import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class CreateUserDTO {
    @NotBlank
    private String username;
    
    @Email
    private String email;
    
    @NotBlank
    private String password;

    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    } 
}
