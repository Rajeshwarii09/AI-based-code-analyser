package com.example.codeanalyser.analysis.model;

/**
 * A normalized finding reported by one of the static analysis tools.
 */
public class CodeIssue {

    private String toolName;
    private int lineNumber;
    private String message;
    private String severity;

    public CodeIssue() {
    }

    public CodeIssue(String toolName, int lineNumber, String message, String severity) {
        this.toolName = toolName;
        this.lineNumber = lineNumber;
        this.message = message;
        this.severity = severity;
    }

    public String getToolName() {
        return toolName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMessage() {
        return message;
    }

    public String getSeverity() {
        return severity;
    }
}
