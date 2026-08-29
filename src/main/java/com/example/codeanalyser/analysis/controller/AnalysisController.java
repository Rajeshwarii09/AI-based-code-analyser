package com.example.codeanalyser.analysis.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.codeanalyser.analysis.model.AnalysisResult;
import com.example.codeanalyser.analysis.repository.AnalysisResultRepository;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    private final AnalysisResultRepository analysisResultRepository;

    public AnalysisController(AnalysisResultRepository analysisResultRepository) {
        this.analysisResultRepository = analysisResultRepository;
    }

    @GetMapping("/results/{snippetId}")
    public ResponseEntity<List<AnalysisResult>> getResultsBySnippet(@PathVariable Long snippetId) {
        try {
            List<AnalysisResult> results = analysisResultRepository.findBySnippetId(snippetId);

            if (results.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Error while fetching analysis results for snippetId {}: {}", snippetId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
