package com.example.codeanalyser.analysis.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Long> kafkaTemplate;
    private static final String TOPIC = "code-snippet-topic";

    public KafkaProducerService(KafkaTemplate<String, Long> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSnippetId(Long snippetId) {
        kafkaTemplate.send(TOPIC, snippetId);
    }
}
