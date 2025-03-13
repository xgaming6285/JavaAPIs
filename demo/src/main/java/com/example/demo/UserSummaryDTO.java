package com.example.demo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean active;

    public static UserSummaryDTO fromUser(User user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .active(user.isActive())
                .build();
    }
} 