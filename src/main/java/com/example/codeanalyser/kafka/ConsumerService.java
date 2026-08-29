package com.example.codeanalyser.kafka;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.codeanalyser.codesnippet.repository.CodeSnippetRepository;
import com.example.codeanalyser.model.CombinedAnalysisResult;
import com.example.codeanalyser.repository.CombinedAnalysisResultRepository;
@Service
public class ConsumerService {
    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private CombinedAnalysisResultRepository combinedAnalysisResultRepository;
    @Autowired
    private CodeSnippetRepository codeSnippetRepository;
    @KafkaListener(topics = "code-snippet-topic", groupId = "code-analyzer-group")
    public void consume(String message) {
        Map<String, Object> msgMap;
        try {
            msgMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(message, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Long snippetId = ((Number) msgMap.get("snippet_id")).longValue();
        String code = (String) msgMap.get("code");
        Map<String, Object> request = Map.of("snippet_id", snippetId, "code", code);
        webClientBuilder.build()
            .post()
            .uri("http://localhost:8000/analyze")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Map.class)
            .subscribe(result -> {
                CombinedAnalysisResult analysisResult = new CombinedAnalysisResult();
                analysisResult.setSnippetId(snippetId);
                analysisResult.setBugReport((String) result.get("details"));
                analysisResult.setAnalyzedAt(LocalDateTime.now());
                combinedAnalysisResultRepository.save(analysisResult);
            }, error -> {
                error.printStackTrace();
            });
    }
}
