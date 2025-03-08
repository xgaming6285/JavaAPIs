package com.example.demo;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class UpdateRolesDTO {
    @NotEmpty(message = "Roles cannot be empty")
    private Set<String> roles;

    public UpdateRolesDTO() {
    }

    public UpdateRolesDTO(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
} 