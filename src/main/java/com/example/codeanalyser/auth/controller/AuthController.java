package com.example.codeanalyser.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.codeanalyser.auth.dto.JwtResponse;
import com.example.codeanalyser.auth.dto.LoginRequest;
import com.example.codeanalyser.auth.dto.RegisterRequest;
import com.example.codeanalyser.auth.model.User;
import com.example.codeanalyser.auth.repository.UserRepository;
import com.example.codeanalyser.auth.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Public GET endpoint for quick access test
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Public test endpoint");
    }

    // Registration endpoint
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully");
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
