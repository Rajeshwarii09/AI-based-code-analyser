package com.example.codeanalyser.analysis.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.codeanalyser.analysis.service.AnalysisService;

@Service
public class AnalysisConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisConsumer.class);

    private final AnalysisService analysisService;

    public AnalysisConsumer(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @KafkaListener(topics = "code-snippet-topic", groupId = "code-analyzer-group")
    public void consume(Long snippetId) {
        logger.info("Received snippet ID for analysis: {}", snippetId);

        try {
            analysisService.analyzeSnippet(snippetId);
            logger.info("Completed analysis for snippet ID: {}", snippetId);
        } catch (Exception e) {
            logger.error("Error analyzing snippet ID: " + snippetId, e);
            // Optionally, implement retry, dead-letter queue logic here
        }
    }
}
