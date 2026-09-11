package com.example.codeanalyser.analysis.model;

import java.util.List;

public class AiInsight {
    private String summary;
    private int qualityScore;
    private List<String> suggestions;

    public AiInsight() {
    }

    public AiInsight(String summary, int qualityScore, List<String> suggestions) {
        this.summary = summary;
        this.qualityScore = qualityScore;
        this.suggestions = suggestions;
    }

    public String getSummary() {
        return summary;
    }

    public int getQualityScore() {
        return qualityScore;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
