package com.example.codeanalyser.analysis.producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.codeanalyser.codesnippet.model.CodeSnippet;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TOPIC = "code-snippet-topic";
    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendSnippet(CodeSnippet snippet) {
        try {
            String json = objectMapper.writeValueAsString(
                new SnippetMessage(snippet.getId(), snippet.getContent())
            );
            kafkaTemplate.send(TOPIC, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static class SnippetMessage {
        public Long snippet_id;
        public String code;
        public SnippetMessage(Long snippet_id, String code) {
            this.snippet_id = snippet_id;
            this.code = code;
        }
    }
}
