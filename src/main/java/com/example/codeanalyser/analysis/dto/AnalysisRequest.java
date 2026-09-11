package com.example.codeanalyser.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnalysisRequest {
    @NotBlank(message = "Language is required")
    private String language = "JAVA";

    @NotBlank(message = "Code content must not be blank")
    @Size(max = 200000, message = "Code content must not exceed 200,000 characters")
    private String code;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
