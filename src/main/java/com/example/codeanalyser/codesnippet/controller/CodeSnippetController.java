package com.example.codeanalyser.codesnippet.controller;

import jakarta.validation.Valid;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.codeanalyser.auth.model.User;
import com.example.codeanalyser.auth.repository.UserRepository;
import com.example.codeanalyser.codesnippet.dto.UploadRequest;
import com.example.codeanalyser.codesnippet.model.CodeSnippet;
import com.example.codeanalyser.codesnippet.service.CodeSnippetService;

/**
 * REST controller for code snippet upload APIs.
 */
@RestController
@RequestMapping("/api/code")
public class CodeSnippetController {

    private final CodeSnippetService codeSnippetService;
    private final UserRepository userRepository;

    public CodeSnippetController(CodeSnippetService codeSnippetService, UserRepository userRepository) {
        this.codeSnippetService = codeSnippetService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCodeSnippet(@Valid @RequestBody UploadRequest uploadRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User user = userOpt.get();
        CodeSnippet savedSnippet = codeSnippetService.saveCodeSnippet(user.getId(), uploadRequest.getContent());

        return ResponseEntity.ok(savedSnippet);
    }
}
