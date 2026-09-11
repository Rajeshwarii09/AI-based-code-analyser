package com.example.codeanalyser.codesnippet.service;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.codeanalyser.analysis.producer.KafkaProducerService;
import com.example.codeanalyser.codesnippet.model.CodeSnippet;
import com.example.codeanalyser.codesnippet.repository.CodeSnippetRepository;
@Service
public class CodeSnippetService {
    private static final Logger logger = LoggerFactory.getLogger(CodeSnippetService.class);
    private final CodeSnippetRepository codeSnippetRepository;
    private final KafkaProducerService kafkaProducerService;
    public CodeSnippetService(CodeSnippetRepository codeSnippetRepository,
                              KafkaProducerService kafkaProducerService) {
        this.codeSnippetRepository = codeSnippetRepository;
        this.kafkaProducerService = kafkaProducerService;
    }
    public CodeSnippet saveCodeSnippet(Long userId, String language, String content) {
        CodeSnippet snippet = new CodeSnippet();
        snippet.setUserId(userId);
        snippet.setContent(content);
        snippet.setLanguage(language);
        snippet.setUploadedAt(LocalDateTime.now());
        snippet.setStatus("UPLOADED");
        CodeSnippet savedSnippet = codeSnippetRepository.save(snippet);
        logger.info("SNIPPET_PERSISTED snippetId={} status={}", savedSnippet.getId(), savedSnippet.getStatus());
        savedSnippet.setStatus("QUEUED");
        CodeSnippet queuedSnippet = codeSnippetRepository.save(savedSnippet);
        kafkaProducerService.sendSnippet(queuedSnippet).whenComplete((result, error) -> {
            if (error != null) {
                queuedSnippet.setStatus("FAILED");
                codeSnippetRepository.save(queuedSnippet);
                logger.error("ANALYSIS_QUEUE_FAILED snippetId={}", queuedSnippet.getId(), error);
            }
        });
        logger.info("ANALYSIS_QUEUED snippetId={} status={}", savedSnippet.getId(), savedSnippet.getStatus());
        return queuedSnippet;
    }

    public List<CodeSnippet> getSnippetsForUser(Long userId) {
        return codeSnippetRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }
}
