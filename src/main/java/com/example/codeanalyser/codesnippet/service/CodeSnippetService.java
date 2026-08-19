package com.example.codeanalyser.codesnippet.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.codeanalyser.analysis.producer.KafkaProducerService;
import com.example.codeanalyser.codesnippet.model.CodeSnippet;
import com.example.codeanalyser.codesnippet.repository.CodeSnippetRepository;

@Service
public class CodeSnippetService {

    private final CodeSnippetRepository codeSnippetRepository;
    private final KafkaProducerService kafkaProducerService;

    public CodeSnippetService(CodeSnippetRepository codeSnippetRepository,
                              KafkaProducerService kafkaProducerService) {
        this.codeSnippetRepository = codeSnippetRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public CodeSnippet saveCodeSnippet(Long userId, String content) {
        CodeSnippet snippet = new CodeSnippet();
        snippet.setUserId(userId);
        snippet.setContent(content);
        snippet.setUploadedAt(LocalDateTime.now());
        snippet.setStatus("NEW");

        CodeSnippet savedSnippet = codeSnippetRepository.save(snippet);

        // Publish snippet ID to Kafka topic for analysis
        // kafkaProducerService.sendSnippetId(savedSnippet.getId());

        return savedSnippet;
    }
}
