package com.example.codeanalyser.codesnippet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for receiving code snippet upload requests with validation.
 */
public class UploadRequest {

    @NotBlank(message = "Code content must not be blank")
    @Size(min = 5, message = "Code content must be at least 5 characters long")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
