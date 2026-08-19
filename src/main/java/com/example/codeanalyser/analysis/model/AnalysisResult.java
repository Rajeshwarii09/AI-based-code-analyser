package com.example.codeanalyser.analysis.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long snippetId;

    @Column(columnDefinition = "TEXT")
    private String result;

    private String status;

    private LocalDateTime analyzedAt;

    // Getters and setters

    public Long getId() { return id; }
    public Long getSnippetId() { return snippetId; }
    public void setSnippetId(Long snippetId) { this.snippetId = snippetId; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
