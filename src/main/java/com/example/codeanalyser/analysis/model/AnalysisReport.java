package com.example.codeanalyser.analysis.model;

import java.util.ArrayList;
import java.util.List;

public class AnalysisReport {
    private String status;
    private String language;
    private List<CodeIssue> issues = new ArrayList<>();
    private List<CompilationDiagnostic> compilationDiagnostics = new ArrayList<>();
    private AiInsight aiInsight;

    public AnalysisReport() {
    }

    public AnalysisReport(String status, String language, List<CodeIssue> issues,
                          List<CompilationDiagnostic> compilationDiagnostics,
                          AiInsight aiInsight) {
        this.status = status;
        this.language = language;
        this.issues = issues;
        this.compilationDiagnostics = compilationDiagnostics;
        this.aiInsight = aiInsight;
    }

    public String getStatus() {
        return status;
    }

    public String getLanguage() {
        return language;
    }

    public List<CodeIssue> getIssues() {
        return issues;
    }

    public List<CompilationDiagnostic> getCompilationDiagnostics() {
        return compilationDiagnostics;
    }

    public AiInsight getAiInsight() {
        return aiInsight;
    }
}
