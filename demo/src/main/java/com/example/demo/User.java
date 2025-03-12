package com.example.demo;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_username_email", columnList = "username,email"),
    @Index(name = "idx_username_active", columnList = "username,active"),
    @Index(name = "idx_email_active", columnList = "email,active")
})
public final class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String token;

    @Column(nullable = false)
    private boolean active = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    protected User() {
        this.id = null;
    }

    /**
     * Creates a new user with basic information.
     *
     * @param username The user's username
     * @param email The user's email address
     * @param password The user's encrypted password
     */
    public User(String username, String email, String password) {
        this.id = null;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Gets the user's ID.
     *
     * @return The user's ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user's ID.
     *
     * @param id The new ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user's username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's username.
     *
     * @param username The new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user's email address.
     *
     * @return The email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email The new email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's password (encrypted).
     *
     * @return The encrypted password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password The new encrypted password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's verification token.
     *
     * @return The verification token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the user's verification token.
     *
     * @param token The new verification token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Checks if the user is active.
     *
     * @return true if the user is active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the user's active status.
     *
     * @param active The new active status
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the user's roles.
     *
     * @return A copy of the user's roles
     */
    public Set<String> getRoles() {
        return new HashSet<>(roles);
    }

    /**
     * Sets the user's roles.
     *
     * @param roles The new set of roles
     */
    public void setRoles(Set<String> roles) {
        this.roles = new HashSet<>(roles);
    }

    /**
     * Adds a role to the user.
     *
     * @param role The role to add
     */
    public void addRole(String role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    /**
     * Removes a role from the user.
     *
     * @param role The role to remove
     */
    public void removeRole(String role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        if (id != null && user.id != null) {
            return Objects.equals(id, user.id);
        }
        if (id == null && user.id == null) {
            return Objects.equals(username, user.username)
                    && Objects.equals(email, user.email)
                    && Objects.equals(password, user.password)
                    && Objects.equals(token, user.token)
                    && active == user.active
                    && Objects.equals(roles, user.roles);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(username, email, password, token, active, roles);
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', email='%s', active=%b, roles=%s}",
            id, username, email, active, roles);
    }

    /**
     * Creates a new UserBuilder instance.
     *
     * @return A new UserBuilder
     */
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static final class UserBuilder {
        private String username;
        private String email;
        private String password;
        private String token;
        private boolean active;
        private Set<String> roles = new HashSet<>();

        /**
         * Sets the username.
         *
         * @param username The username to set
         * @return This builder instance
         */
        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * Sets the email address.
         *
         * @param email The email to set
         * @return This builder instance
         */
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        /**
         * Sets the password.
         *
         * @param password The password to set
         * @return This builder instance
         */
        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the verification token.
         *
         * @param token The token to set
         * @return This builder instance
         */
        public UserBuilder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * Sets the active status.
         *
         * @param active The active status to set
         * @return This builder instance
         */
        public UserBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        /**
         * Sets the roles.
         *
         * @param roles The roles to set
         * @return This builder instance
         */
        public UserBuilder roles(Set<String> roles) {
            this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
            return this;
        }

        /**
         * Builds a new User instance.
         *
         * @return A new User instance
         */
        public User build() {
            User user = new User(username, email, password);
            user.setToken(token);
            user.setActive(active);
            user.setRoles(new HashSet<>(roles));
            return user;
        }
    }

    /**
     * Converts this User to a UserDTO.
     *
     * @return A UserDTO representing this user's public information
     */
    public UserDTO toDTO() {
        return new UserDTO(this.id, this.username, this.email);
    }

    /**
     * Creates a User instance from a UserDTO.
     * Note: This will only set the id, username, and email fields.
     *
     * @param dto The UserDTO to convert
     * @return A new User instance
     */
    public static User fromDTO(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        return user;
    }
}
