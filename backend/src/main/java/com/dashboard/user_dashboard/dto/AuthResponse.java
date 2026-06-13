package com.dashboard.user_dashboard.dto;

import com.dashboard.user_dashboard.model.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNo;
    
    @JsonProperty("Address")
    private String Address;
    
    private Role role;
}
