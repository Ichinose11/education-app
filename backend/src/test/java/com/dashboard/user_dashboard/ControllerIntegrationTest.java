package com.dashboard.user_dashboard;

import com.dashboard.user_dashboard.dto.LoginRequest;
import com.dashboard.user_dashboard.dto.RegisterRequest;
import com.dashboard.user_dashboard.model.Role;
import com.dashboard.user_dashboard.model.User;
import com.dashboard.user_dashboard.repository.UserRepository;
import com.dashboard.user_dashboard.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class ControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private User adminUser;
    private User regularUser;
    private String adminToken;
    private String userToken;
    private String moderatorToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        userRepository.deleteAllInBatch();

        // Save a regular user
        regularUser = User.builder()
                .username("regularuser")
                .password(passwordEncoder.encode("password123"))
                .firstName("Regular")
                .lastName("User")
                .phoneNo("1234567")
                .Address("User Address")
                .role(Role.USER)
                .build();
        regularUser = userRepository.save(regularUser);

        // Save an admin user
        adminUser = User.builder()
                .username("adminuser")
                .password(passwordEncoder.encode("password123"))
                .firstName("Admin")
                .lastName("User")
                .phoneNo("7654321")
                .Address("Admin Address")
                .role(Role.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);

        // Flush changes to ensure IDs and unique constraints are established properly
        userRepository.flush();

        // Generate tokens
        userToken = jwtTokenProvider.generateToken(regularUser.getUsername(), regularUser.getRole().name());
        adminToken = jwtTokenProvider.generateToken(adminUser.getUsername(), adminUser.getRole().name());
        moderatorToken = jwtTokenProvider.generateToken("moderator", Role.MODERATOR.name());
    }

    @Test
    void testPublicRegisterSuccess() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .phoneNo("111222")
                .Address("New Address")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.firstName", is("New")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.phoneNo", is("111222")))
                .andExpect(jsonPath("$.Address", is("New Address")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void testPublicLoginSuccess() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("regularuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("regularuser")))
                .andExpect(jsonPath("$.firstName", is("Regular")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void testGetUsersAdminAndModeratorOnly() throws Exception {
        // Admin user should access and see all users
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].username", containsInAnyOrder("regularuser", "adminuser")));

        // Moderator user should access and see all users
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Regular user should get Forbidden (403)
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateUserAdminOnly() throws Exception {
        java.util.Map<String, Object> newUser = new java.util.HashMap<>();
        newUser.put("username", "createdbyadmin");
        newUser.put("password", "pass123");
        newUser.put("firstName", "Created");
        newUser.put("lastName", "Admin");
        newUser.put("phoneNo", "999");
        newUser.put("Address", "Created Address");
        newUser.put("role", "USER");

        // Admin can create
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("createdbyadmin")))
                .andExpect(jsonPath("$.createdBy", is("adminuser")))
                .andExpect(jsonPath("$.updateBy", is("adminuser")));

        // Moderator cannot create (Forbidden)
        java.util.Map<String, Object> newUser2 = new java.util.HashMap<>();
        newUser2.put("username", "createdbymod");
        newUser2.put("password", "pass123");
        newUser2.put("role", "USER");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + moderatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateUserAdminAndModeratorOnly() throws Exception {
        User updateDetails = User.builder()
                .firstName("UpdatedFirst")
                .lastName("UpdatedLast")
                .phoneNo("0000")
                .Address("Updated Address")
                .active(false)
                .build();

        // Admin updates details
        mockMvc.perform(put("/api/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("UpdatedFirst")))
                .andExpect(jsonPath("$.lastName", is("UpdatedLast")))
                .andExpect(jsonPath("$.phoneNo", is("0000")))
                .andExpect(jsonPath("$.Address", is("Updated Address")))
                .andExpect(jsonPath("$.active", is(false)))
                .andExpect(jsonPath("$.updateBy", is("adminuser")));

        // Moderator updates details
        User updateDetailsMod = User.builder()
                .firstName("ModUpdatedFirst")
                .lastName("ModUpdatedLast")
                .phoneNo("7777")
                .Address("Mod Address")
                .active(true)
                .build();

        mockMvc.perform(put("/api/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + moderatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetailsMod)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("ModUpdatedFirst")))
                .andExpect(jsonPath("$.lastName", is("ModUpdatedLast")))
                .andExpect(jsonPath("$.phoneNo", is("7777")))
                .andExpect(jsonPath("$.Address", is("Mod Address")))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.updateBy", is("moderator")));

        // Regular user cannot update
        mockMvc.perform(put("/api/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteUserAdminOnly() throws Exception {
        // Regular user attempts to delete: Forbidden (403)
        mockMvc.perform(delete("/api/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Moderator attempts to delete: Forbidden (403)
        mockMvc.perform(delete("/api/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isForbidden());

        // Admin user deletes regular user: 204 No Content
        mockMvc.perform(delete("/api/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetAdminStatsAdminAndModeratorOnly() throws Exception {
        // Regular user attempts to view stats: Forbidden (403)
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Admin user views stats: 200 OK
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(2)))
                .andExpect(jsonPath("$.usersByRole.USER", is(1)))
                .andExpect(jsonPath("$.usersByRole.ADMIN", is(1)));

        // Moderator user views stats: 200 OK
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isOk());
    }
}
