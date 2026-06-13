package com.dashboard.user_dashboard;

import com.dashboard.user_dashboard.dto.AuthResponse;
import com.dashboard.user_dashboard.dto.LoginRequest;
import com.dashboard.user_dashboard.dto.RegisterRequest;
import com.dashboard.user_dashboard.exception.DuplicateResourceException;
import com.dashboard.user_dashboard.exception.InvalidCredentialsException;
import com.dashboard.user_dashboard.model.Role;
import com.dashboard.user_dashboard.model.User;
import com.dashboard.user_dashboard.repository.UserRepository;
import com.dashboard.user_dashboard.security.JwtTokenProvider;
import com.dashboard.user_dashboard.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterAndLoginSuccess() {
        // 1. Register a new user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("securePassword123")
                .firstName("Test")
                .lastName("User")
                .phoneNo("1234567890")
                .Address("123 Main St")
                .build();

        AuthResponse registerResponse = authService.register(registerRequest);

        assertNotNull(registerResponse);
        assertNotNull(registerResponse.getToken());
        assertEquals("testuser", registerResponse.getUsername());
        assertEquals("Test", registerResponse.getFirstName());
        assertEquals("User", registerResponse.getLastName());
        assertEquals("1234567890", registerResponse.getPhoneNo());
        assertEquals("123 Main St", registerResponse.getAddress());
        assertEquals(Role.USER, registerResponse.getRole());

        // Verify user is in database
        assertTrue(userRepository.findByUsername("testuser").isPresent());
        User user = userRepository.findByUsername("testuser").get();
        assertEquals("testuser", user.getUsername());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("1234567890", user.getPhoneNo());
        assertEquals("123 Main St", user.getAddress());
        assertEquals(Role.USER, user.getRole());
        assertTrue(user.getActive());
        assertEquals("self", user.getCreatedBy());
        assertEquals("self", user.getUpdateBy());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());

        // Verify token is valid
        assertTrue(jwtTokenProvider.validateToken(registerResponse.getToken()));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(registerResponse.getToken()));
        assertEquals("USER", jwtTokenProvider.getRoleFromToken(registerResponse.getToken()));

        // 2. Login with the registered user
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("securePassword123")
                .build();

        AuthResponse loginResponse = authService.login(loginRequest);

        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals("testuser", loginResponse.getUsername());
        assertEquals("Test", loginResponse.getFirstName());
        assertEquals("User", loginResponse.getLastName());
        assertEquals("1234567890", loginResponse.getPhoneNo());
        assertEquals("123 Main St", loginResponse.getAddress());
        assertEquals(Role.USER, loginResponse.getRole());

        // Verify token is valid
        assertTrue(jwtTokenProvider.validateToken(loginResponse.getToken()));
    }

    @Test
    void testRegisterDuplicateUsernameThrowsException() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("alice")
                .password("password")
                .firstName("Alice")
                .lastName("Smith")
                .phoneNo("1112223333")
                .Address("Address Alice")
                .build();

        authService.register(registerRequest);

        // Try registering again with same username
        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(registerRequest);
        });
    }

    @Test
    void testLoginWithInvalidCredentialsThrowsException() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("bob")
                .password("password123")
                .firstName("Bob")
                .lastName("Jones")
                .phoneNo("4445556666")
                .Address("Address Bob")
                .build();

        authService.register(registerRequest);

        LoginRequest invalidLogin = LoginRequest.builder()
                .username("bob")
                .password("wrongpassword")
                .build();

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(invalidLogin);
        });
    }

    @Test
    void testHardcodedAdminAndModeratorLogin() {
        // Test admin login
        LoginRequest adminLogin = LoginRequest.builder()
                .username("admin")
                .password("adminpassword")
                .build();

        AuthResponse adminResponse = authService.login(adminLogin);
        assertNotNull(adminResponse);
        assertNotNull(adminResponse.getToken());
        assertEquals("admin", adminResponse.getUsername());
        assertEquals("System", adminResponse.getFirstName());
        assertEquals("Admin", adminResponse.getLastName());
        assertEquals(Role.ADMIN, adminResponse.getRole());
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), adminResponse.getId());

        // Test moderator login
        LoginRequest moderatorLogin = LoginRequest.builder()
                .username("moderator")
                .password("moderatorpassword")
                .build();

        AuthResponse moderatorResponse = authService.login(moderatorLogin);
        assertNotNull(moderatorResponse);
        assertNotNull(moderatorResponse.getToken());
        assertEquals("moderator", moderatorResponse.getUsername());
        assertEquals("System", moderatorResponse.getFirstName());
        assertEquals("Moderator", moderatorResponse.getLastName());
        assertEquals(Role.MODERATOR, moderatorResponse.getRole());
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000002"), moderatorResponse.getId());
    }
}
