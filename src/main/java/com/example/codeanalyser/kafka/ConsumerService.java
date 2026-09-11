package com.example.codeanalyser.kafka;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.codeanalyser.analysis.model.AnalysisResult;
import com.example.codeanalyser.analysis.model.AnalysisReport;
import com.example.codeanalyser.analysis.service.CodeAnalysisService;
import com.example.codeanalyser.analysis.repository.AnalysisResultRepository;
import com.example.codeanalyser.codesnippet.model.CodeSnippet;
import com.example.codeanalyser.codesnippet.repository.CodeSnippetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerService.class);

    private final CodeAnalysisService codeAnalysisService;
    private final AnalysisResultRepository analysisResultRepository;
    private final CodeSnippetRepository codeSnippetRepository;
    private final ObjectMapper objectMapper;

    public ConsumerService(CodeAnalysisService codeAnalysisService,
                           AnalysisResultRepository analysisResultRepository,
                           CodeSnippetRepository codeSnippetRepository) {
        this.codeAnalysisService = codeAnalysisService;
        this.analysisResultRepository = analysisResultRepository;
        this.codeSnippetRepository = codeSnippetRepository;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "code-snippet-topic", groupId = "code-analyzer-group")
    public void consume(String message) {
        logger.info("ANALYSIS_MESSAGE_RECEIVED rawMessageLength={}", message.length());
        Map<String, Object> msgMap;
        try {
            msgMap = objectMapper.readValue(message, Map.class);
        } catch (JsonProcessingException e) {
            logger.error("ANALYSIS_MESSAGE_INVALID unable to parse Kafka JSON", e);
            return;
        }

        if (msgMap == null || !msgMap.containsKey("snippet_id") || !msgMap.containsKey("code")
            || !msgMap.containsKey("language")) {
            logger.error("ANALYSIS_MESSAGE_INVALID missing snippet_id, language, or code");
            return;
        }

        Long snippetId;
        String code;
        String language;
        try {
            snippetId = ((Number) msgMap.get("snippet_id")).longValue();
            code = (String) msgMap.get("code");
            language = (String) msgMap.get("language");
        } catch (ClassCastException e) {
            System.err.println("Invalid data types in message: " + message);
            e.printStackTrace();
            return;
        }

        CodeSnippet snippet = codeSnippetRepository.findById(snippetId)
            .orElseThrow(() -> new IllegalStateException("Snippet not found: " + snippetId));
        snippet.setStatus("ANALYZING");
        codeSnippetRepository.save(snippet);
        logger.info("ANALYSIS_STARTED snippetId={} analyzer=PMD-SpotBugs-Checkstyle", snippetId);

        try {
            AnalysisReport report = codeAnalysisService.analyzeCode(language, code);
            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setSnippetId(snippetId);
            analysisResult.setResult(objectMapper.writeValueAsString(report));
            analysisResult.setStatus("COMPLETED");
            analysisResult.setAnalyzedAt(LocalDateTime.now());
            analysisResultRepository.save(analysisResult);
            snippet.setStatus("ANALYZED");
            codeSnippetRepository.save(snippet);
            logger.info("ANALYSIS_COMPLETED snippetId={} findings={} compilationDiagnostics={}",
                snippetId, report.getIssues().size(), report.getCompilationDiagnostics().size());
        } catch (Exception exception) {
            AnalysisResult failure = new AnalysisResult();
            failure.setSnippetId(snippetId);
            failure.setResult(exception.getMessage());
            failure.setStatus("FAILED");
            failure.setAnalyzedAt(LocalDateTime.now());
            analysisResultRepository.save(failure);
            snippet.setStatus("FAILED");
            codeSnippetRepository.save(snippet);
            logger.error("ANALYSIS_FAILED snippetId={}", snippetId, exception);
        }
    }
}
