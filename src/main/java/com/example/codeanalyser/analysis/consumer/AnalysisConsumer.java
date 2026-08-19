package com.example.codeanalyser.analysis.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AnalysisConsumer {

    @KafkaListener(topics = "code-snippet-topic", groupId = "code-analyzer-group")
    public void consume(Long snippetId) {
        System.out.println("Received snippet ID to analyze: " + snippetId);

        // TODO: Implement code analysis logic here

        // After analysis, save results via AnalysisService/repository
    }
}
