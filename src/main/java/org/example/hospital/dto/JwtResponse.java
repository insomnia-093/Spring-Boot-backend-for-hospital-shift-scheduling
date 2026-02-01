package org.example.hospital.dto;

import java.util.Set;
import org.example.hospital.domain.RoleType;

public class JwtResponse {

    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private Set<RoleType> roles;

    public JwtResponse(String token, Long userId, String email, String fullName, Set<RoleType> roles) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public Set<RoleType> getRoles() {
        return roles;
    }
}
