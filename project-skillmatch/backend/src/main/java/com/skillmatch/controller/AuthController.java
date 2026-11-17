package com.skillmatch.controller;

import com.skillmatch.model.User;
import com.skillmatch.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            User.Role role = User.Role.valueOf(body.get("role").toUpperCase());
            String rollNumber = body.get("rollNumber");
            User user = authService.register(
                    body.get("email"),
                    body.get("password"),
                    body.get("fullName"),
                    role,
                    rollNumber
            );
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("id", user.getId());
            resp.put("email", user.getEmail());
            resp.put("fullName", user.getFullName());
            resp.put("role", user.getRole().name());
            if (user.getRollNumber() != null) {
                resp.put("rollNumber", user.getRollNumber());
            }
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String identifier = body.get("email");
        if (identifier == null || identifier.isBlank()) {
            identifier = body.get("loginId");
        }
        String password = body.get("password");

        Optional<User> user = authService.login(identifier, password);
        if (user.isPresent()) {
            User u = user.get();
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("id", u.getId());
            resp.put("email", u.getEmail());
            resp.put("fullName", u.getFullName());
            resp.put("role", u.getRole().name());
            if (u.getRollNumber() != null) {
                resp.put("rollNumber", u.getRollNumber());
            }
            return ResponseEntity.ok(resp);
        }
        return ResponseEntity.status(401).body(Map.of(
                "error", "Invalid credentials. Use roll number (e.g. 25R11A66L3) or email with Password123"
        ));
    }
}
