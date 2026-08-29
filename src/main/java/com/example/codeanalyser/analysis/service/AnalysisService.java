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

    // Constructor injection of repositories for database access
    public AnalysisService(CodeSnippetRepository codeSnippetRepository,
                           AnalysisResultRepository analysisResultRepository) {
        this.codeSnippetRepository = codeSnippetRepository;
        this.analysisResultRepository = analysisResultRepository;
    }

    // Method to analyze a code snippet by its ID
    public void analyzeSnippet(Long snippetId) {

        // Line 1: Fetch the snippet from DB using the snippet id
        Optional<CodeSnippet> snippetOptional = codeSnippetRepository.findById(snippetId);

        // Line 2: If snippet not found, stop processing
        if (snippetOptional.isEmpty()) {
            System.err.println("Snippet not found: ID = " + snippetId);
            return;
        }

        CodeSnippet snippet = snippetOptional.get();

        // Line 3: Simple dummy analysis - count lines in the code snippet
        String code = snippet.getContent();
        int lineCount = code.split("\\r?\\n").length;

        // Line 4: Prepare the analysis result text
        String analysisResult = "Code length: " + code.length() + " characters; Number of lines: " + lineCount;

        // Line 5: Create a new AnalysisResult entity and set the results
        AnalysisResult result = new AnalysisResult();
        result.setSnippetId(snippetId);
        result.setResult(analysisResult);        // Store the analysis message
        result.setStatus("COMPLETED");
        result.setAnalyzedAt(LocalDateTime.now());

        // Line 6: Save the analysis result in the database
        analysisResultRepository.save(result);

        // Line 7: Update snippet status to "ANALYZED"
        snippet.setStatus("ANALYZED");
        codeSnippetRepository.save(snippet);

        System.out.println("Analysis completed for snippet ID: " + snippetId);
    }
}
