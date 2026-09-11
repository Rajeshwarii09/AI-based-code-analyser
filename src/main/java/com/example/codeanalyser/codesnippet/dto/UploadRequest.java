package com.example.codeanalyser.codesnippet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for receiving code snippet upload requests with validation.
 */
public class UploadRequest {
    @NotBlank(message = "Language is required")
    private String language = "JAVA";

    @NotBlank(message = "Code content must not be blank")
    @Size(min = 5, max = 200000,
          message = "Code content must be between 5 and 200,000 characters")
    private String content;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
