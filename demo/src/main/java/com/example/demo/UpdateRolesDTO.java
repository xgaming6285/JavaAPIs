package com.example.demo;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public class UpdateRolesDTO {
    @NotEmpty(message = "Roles cannot be empty")
    private Set<String> roles;

    public UpdateRolesDTO() {
        this.roles = new HashSet<>();
    }

    public UpdateRolesDTO(Set<String> roles) {
        setRoles(roles);  // Use setter to ensure defensive copy
    }

    public Set<String> getRoles() {
        return roles != null ? Collections.unmodifiableSet(new HashSet<>(roles)) : Collections.emptySet();
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }
} 