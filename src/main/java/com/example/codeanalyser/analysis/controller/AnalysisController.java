package com.example.codeanalyser.analysis.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.codeanalyser.analysis.model.AnalysisResult;
import com.example.codeanalyser.analysis.model.AnalysisReport;
import com.example.codeanalyser.analysis.dto.AnalysisRequest;
import com.example.codeanalyser.analysis.repository.AnalysisResultRepository;
import com.example.codeanalyser.analysis.service.CodeAnalysisService;
import com.example.codeanalyser.auth.model.User;
import com.example.codeanalyser.auth.repository.UserRepository;
import com.example.codeanalyser.codesnippet.repository.CodeSnippetRepository;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    private final AnalysisResultRepository analysisResultRepository;
    private final CodeAnalysisService codeAnalysisService;
    private final CodeSnippetRepository codeSnippetRepository;
    private final UserRepository userRepository;

    public AnalysisController(AnalysisResultRepository analysisResultRepository,
                              CodeAnalysisService codeAnalysisService,
                              CodeSnippetRepository codeSnippetRepository,
                              UserRepository userRepository) {
        this.analysisResultRepository = analysisResultRepository;
        this.codeAnalysisService = codeAnalysisService;
        this.codeSnippetRepository = codeSnippetRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/code")
    public ResponseEntity<AnalysisReport> analyzeCode(@Valid @RequestBody AnalysisRequest request) {
        return ResponseEntity.ok(codeAnalysisService.analyzeCode(
            request.getLanguage(), request.getCode()));
    }

    @GetMapping("/results/{snippetId}")
    public ResponseEntity<List<AnalysisResult>> getResultsBySnippet(@PathVariable Long snippetId) {
        try {
            logger.info("ANALYSIS_RESPONSE_REQUESTED snippetId={}", snippetId);
            var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
            if (codeSnippetRepository.findById(snippetId)
                .filter(snippet -> snippet.getUserId().equals(user.getId())).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            List<AnalysisResult> results = analysisResultRepository.findBySnippetId(snippetId);

            if (results.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            logger.info("RESPONSE_GENERATED stage=ANALYSIS_COMPLETE snippetId={} findings={}",
                        snippetId, results.size());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error while fetching analysis results for snippetId {}: {}", snippetId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
