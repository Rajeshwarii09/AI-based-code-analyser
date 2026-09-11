package com.example.codeanalyser.analysis.producer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;

import com.example.codeanalyser.codesnippet.model.CodeSnippet;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TOPIC = "code-snippet-topic";
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public CompletableFuture<SendResult<String, String>> sendSnippet(CodeSnippet snippet) {
        try {
            String json = objectMapper.writeValueAsString(
                new SnippetMessage(snippet.getId(), snippet.getLanguage(), snippet.getContent())
            );
            return kafkaTemplate.send(TOPIC, json).whenComplete((result, error) -> {
                if (error != null) {
                    logger.error("ANALYSIS_QUEUE_FAILED snippetId={}", snippet.getId(), error);
                } else {
                    logger.info("ANALYSIS_MESSAGE_SENT snippetId={} topic={}", snippet.getId(), TOPIC);
                }
            });
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize snippet for analysis", exception);
        }
    }
    private static class SnippetMessage {
        public Long snippet_id;
        public String language;
        public String code;
        public SnippetMessage(Long snippet_id, String language, String code) {
            this.snippet_id = snippet_id;
            this.language = language;
            this.code = code;
        }
    }
}
