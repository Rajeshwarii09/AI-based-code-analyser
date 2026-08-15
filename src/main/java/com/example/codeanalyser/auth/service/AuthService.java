package com.example.codeanalyser.auth.service;

import com.example.codeanalyser.auth.model.User;
import com.example.codeanalyser.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String rawPassword) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Username already taken.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User newUser = new User(username, encodedPassword);

        return userRepository.save(newUser);
    }
}
