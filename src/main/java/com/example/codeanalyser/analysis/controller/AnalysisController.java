package com.example.codeanalyser.analysis.controller;

import java.util.List;

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

    private final AnalysisResultRepository analysisResultRepository;

    public AnalysisController(AnalysisResultRepository analysisResultRepository) {
        this.analysisResultRepository = analysisResultRepository;
    }

    // GET /api/analysis/results/{snippetId}
    @GetMapping("/results/{snippetId}")
    public ResponseEntity<List<AnalysisResult>> getResultsBySnippet(@PathVariable Long snippetId) {
        List<AnalysisResult> results = analysisResultRepository.findBySnippetId(snippetId);

        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(results);
    }
}
