package com.Assignemnt.Linkedin.Clone.controller;

import com.Assignemnt.Linkedin.Clone.model.User;
import com.Assignemnt.Linkedin.Clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")

public class AuthController {

    private final UserRepository userRepo;

    // Utility method for SHA-256 password hashing
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Signup API
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already exists"));
        }

        // Hash the password before saving
        user.setPassword(hashPassword(user.getPassword()));
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    // Login API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String email = body.get("email");
        String password = body.get("password");

        var optUser = userRepo.findByEmail(email);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        User user = optUser.get();
        String hashedPassword = hashPassword(password);

        if (!user.getPassword().equals(hashedPassword)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        // Store session data
        session.setAttribute("USER_EMAIL", user.getEmail());
        session.setAttribute("USER_ID", user.getId());
        session.setAttribute("USER_NAME", user.getName());

        return ResponseEntity.ok(Map.of(
                "message", "Logged in successfully",
                "name", user.getName(),
                "email", user.getEmail()
        ));
    }

    // Logout API
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // Current logged-in user API
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object email = session.getAttribute("USER_EMAIL");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not logged in"));
        }

        return ResponseEntity.ok(Map.of(
                "email", email,
                "name", session.getAttribute("USER_NAME")
        ));
    }
}
