package com.dashboard.user_dashboard.dto;

import com.dashboard.user_dashboard.model.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNo;
    
    @JsonProperty("Address")
    private String Address;
    
    private Role role; // optional, can be null
}
