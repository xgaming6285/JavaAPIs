package com.example.demo;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Objects;

public class UpdateRolesDTO {
    /** Set of roles for the user. Cannot be empty. */
    @NotEmpty(message = "Roles cannot be empty")
    private Set<String> roles;

    public UpdateRolesDTO() {
        this.roles = new HashSet<>();
    }

    /** 
     * Creates an UpdateRolesDTO with the specified roles.
     *
     * @param roles the set of roles to assign
     */
    public UpdateRolesDTO(Set<String> roles) {
        this.roles = new HashSet<>(Objects.requireNonNull(roles, "Roles must not be null"));
    }

    public Set<String> getRoles() {
        return roles != null ? Collections.unmodifiableSet(new HashSet<>(roles)) : Collections.emptySet();
    }

    /**
     * Sets the roles for this DTO.
     *
     * @param roles the set of roles to assign
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }
} 