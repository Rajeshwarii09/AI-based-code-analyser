package com.example.codeanalyser.analysis.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.codeanalyser.analysis.model.AnalysisResult;
import com.example.codeanalyser.analysis.repository.AnalysisResultRepository;
import com.example.codeanalyser.codesnippet.model.CodeSnippet;
import com.example.codeanalyser.codesnippet.repository.CodeSnippetRepository;

@Service
public class AnalysisService {

    private final CodeSnippetRepository codeSnippetRepository;
    private final AnalysisResultRepository analysisResultRepository;

    public AnalysisService(CodeSnippetRepository codeSnippetRepository,
                           AnalysisResultRepository analysisResultRepository) {
        this.codeSnippetRepository = codeSnippetRepository;
        this.analysisResultRepository = analysisResultRepository;
    }

    public void analyzeSnippet(Long snippetId) {
        Optional<CodeSnippet> snippetOpt = codeSnippetRepository.findById(snippetId);

        if (snippetOpt.isEmpty()) {
            System.err.println("Code snippet not found for ID: " + snippetId);
            return;
        }

        CodeSnippet snippet = snippetOpt.get();

        // Simulate analysis logic (replace this with your real AI/static analysis)
        String analysisResultText = performDummyAnalysis(snippet.getContent());

        // Save analysis result to database
        AnalysisResult result = new AnalysisResult();
        result.setSnippetId(snippetId);
        result.setResult(analysisResultText);
        result.setStatus("COMPLETED");
        result.setAnalyzedAt(LocalDateTime.now());

        analysisResultRepository.save(result);

        // Optionally update snippet status
        snippet.setStatus("ANALYZED");
        codeSnippetRepository.save(snippet);

        System.out.println("Analysis completed for snippet ID: " + snippetId);
    }

    private String performDummyAnalysis(String code) {
        // Simple dummy check: count lines
        int lines = code.split("\\r?\\n").length;
        return "Code analysis: Number of lines = " + lines;
    }
}
