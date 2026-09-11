package com.example.codeanalyser.analysis.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.codeanalyser.analysis.model.AiInsight;
import com.example.codeanalyser.analysis.model.CodeIssue;
import com.example.codeanalyser.analysis.model.CompilationDiagnostic;

/**
 * Local, deterministic AI adapter. Replace this implementation with an HTTP
 * client when an approved model provider is configured.
 */
@Service
public class AiAnalysisService {

    public AiInsight explain(List<CodeIssue> issues, List<CompilationDiagnostic> diagnostics) {
        int problemCount = issues.size() + diagnostics.size();
        int score = Math.max(0, 100 - problemCount * 8);
        List<String> suggestions = new ArrayList<>();

        if (!diagnostics.isEmpty()) {
            suggestions.add("Fix compilation errors first because static-analysis results may be incomplete.");
        }
        if (!issues.isEmpty()) {
            suggestions.add("Review each finding and prefer small, testable changes.");
        }
        if (issues.isEmpty() && diagnostics.isEmpty()) {
            suggestions.add("No problems were detected. Keep the code covered by tests and review security-sensitive changes.");
        }

        String summary = problemCount == 0
            ? "The snippet compiled successfully and no analyzer violations were found."
            : "The analysis found " + problemCount + " item(s) to review.";
        return new AiInsight(summary, score, suggestions);
    }
}
