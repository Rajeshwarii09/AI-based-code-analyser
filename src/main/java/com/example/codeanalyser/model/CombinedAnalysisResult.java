package com.example.codeanalyser.model;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
@Entity
public class CombinedAnalysisResult {
    @Id @GeneratedValue
    private Long id;
    private Long snippetId;
    private String bugReport;
    private LocalDateTime analyzedAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSnippetId() { return snippetId; }
    public void setSnippetId(Long snippetId) { this.snippetId = snippetId; }
    public String getBugReport() { return bugReport; }
    public void setBugReport(String bugReport) { this.bugReport = bugReport; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
