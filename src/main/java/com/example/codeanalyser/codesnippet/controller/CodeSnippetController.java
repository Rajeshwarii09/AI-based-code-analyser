package com.example.codeanalyser.codesnippet.controller;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(CodeSnippetController.class);
    private final CodeSnippetService codeSnippetService;
    private final UserRepository userRepository;

    public CodeSnippetController(CodeSnippetService codeSnippetService, UserRepository userRepository) {
        this.codeSnippetService = codeSnippetService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCodeSnippet(@Valid @RequestBody UploadRequest uploadRequest) {
        logger.info("UPLOAD_RECEIVED language={} contentLength={}",
            uploadRequest.getLanguage(), uploadRequest.getContent().length());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User user = userOpt.get();
        CodeSnippet savedSnippet = codeSnippetService.saveCodeSnippet(
            user.getId(), uploadRequest.getLanguage(), uploadRequest.getContent());

        logger.info("RESPONSE_GENERATED stage=UPLOADED snippetId={} status={}",
                    savedSnippet.getId(), savedSnippet.getStatus());
        return ResponseEntity.ok(savedSnippet);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getUploadHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userRepository.findByUsername(auth.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        List<CodeSnippet> history =
            codeSnippetService.getSnippetsForUser(userOpt.get().getId());
        return ResponseEntity.ok(history);
    }
}
