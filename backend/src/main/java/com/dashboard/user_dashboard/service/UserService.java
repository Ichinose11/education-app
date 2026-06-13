package com.dashboard.user_dashboard.service;

import com.dashboard.user_dashboard.dto.AdminStatsResponse;
import com.dashboard.user_dashboard.model.Role;
import com.dashboard.user_dashboard.model.User;
import com.dashboard.user_dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(User user, String adminUsername) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username '" + user.getUsername() + "' is already taken.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedBy(adminUsername);
        user.setUpdateBy(adminUsername);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, User userDetails, String updaterUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhoneNo(userDetails.getPhoneNo());
        user.setAddress(userDetails.getAddress());
        user.setActive(userDetails.getActive());
        user.setUpdateBy(updaterUsername);
        user.setUpdatedAt(Instant.now());
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats() {
        long totalUsers = userRepository.count();
        
        Map<String, Long> usersByRole = new HashMap<>();
        for (Role role : Role.values()) {
            usersByRole.put(role.name(), userRepository.countByRole(role));
        }

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .usersByRole(usersByRole)
                .build();
    }
}
