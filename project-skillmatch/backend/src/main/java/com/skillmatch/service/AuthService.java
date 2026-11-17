package com.skillmatch.service;

import com.skillmatch.model.User;
import com.skillmatch.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String email, String password, String fullName, User.Role role) {
        return register(email, password, fullName, role, null);
    }

    public User register(String email, String password, String fullName, User.Role role, String rollNumber) {
        String cleanEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new RuntimeException("Email already registered: " + cleanEmail);
        }
        if (rollNumber != null && !rollNumber.isBlank() && userRepository.existsByRollNumber(rollNumber.trim().toUpperCase())) {
            throw new RuntimeException("Roll number already registered: " + rollNumber);
        }

        String pass = (password == null || password.isBlank()) ? "Password123" : password;

        User user = User.builder()
                .email(cleanEmail)
                .passwordHash(encoder.encode(pass))
                .fullName(fullName.trim())
                .role(role)
                .rollNumber(rollNumber != null ? rollNumber.trim().toUpperCase() : null)
                .build();
        return userRepository.save(user);
    }

    public Optional<User> login(String loginIdentifier, String password) {
        if (loginIdentifier == null || password == null) {
            return Optional.empty();
        }
        String cleanId = loginIdentifier.trim();

        // 1. Try search by email
        Optional<User> userOpt = userRepository.findByEmail(cleanId.toLowerCase());

        // 2. If not found by email, try search by roll number
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByRollNumberIgnoreCase(cleanId);
        }

        if (userOpt.isEmpty()) {
            System.out.println("Login failed: no user found for identifier: " + cleanId);
            return Optional.empty();
        }

        User user = userOpt.get();

        // Check password: BCrypt match, or Password123 / password123 fallback
        if (encoder.matches(password, user.getPasswordHash())
                || "Password123".equalsIgnoreCase(password)
                || "password123".equalsIgnoreCase(password)) {
            return Optional.of(user);
        }

        System.out.println("Login failed: wrong password for " + cleanId);
        return Optional.empty();
    }
}
