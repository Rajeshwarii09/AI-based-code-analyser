package com.example.codeanalyser.codesnippet.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.codeanalyser.auth.model.User;
import com.example.codeanalyser.auth.repository.UserRepository;
import com.example.codeanalyser.codesnippet.model.CodeSnippet;
import com.example.codeanalyser.codesnippet.service.CodeSnippetService;

@RestController
@RequestMapping("/api/code")
public class CodeSnippetController {

    private final CodeSnippetService codeSnippetService;
    private final UserRepository userRepository;

    @Autowired
    public CodeSnippetController(CodeSnippetService codeSnippetService, UserRepository userRepository) {
        this.codeSnippetService = codeSnippetService;
        this.userRepository = userRepository;
    }

    // POST /api/code/upload
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCodeSnippet(@RequestBody Map<String, String> requestBody) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        String content = requestBody.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Code snippet content cannot be empty");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User user = userOpt.get();
        CodeSnippet savedSnippet = codeSnippetService.saveCodeSnippet(user.getId(), content);
        return ResponseEntity.ok(savedSnippet);
    }
}
