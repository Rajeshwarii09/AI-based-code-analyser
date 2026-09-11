package com.example.codeanalyser.analysis.model;

public class CompilationDiagnostic {
    private String kind;
    private long lineNumber;
    private long columnNumber;
    private String message;

    public CompilationDiagnostic() {
    }

    public CompilationDiagnostic(String kind, long lineNumber, long columnNumber, String message) {
        this.kind = kind;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.message = message;
    }

    public String getKind() {
        return kind;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public long getColumnNumber() {
        return columnNumber;
    }

    public String getMessage() {
        return message;
    }
}
