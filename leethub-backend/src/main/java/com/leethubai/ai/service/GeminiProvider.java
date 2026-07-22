package com.leethubai.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leethubai.common.exception.AiGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Google Gemini API provider (used as fallback when OpenAI is unavailable).
 * Uses gemini-1.5-flash for cost-efficient generation.
 */
@Slf4j
@Service
public class GeminiProvider implements AiProvider {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final PromptTemplateService promptTemplateService;
    private final String model;
    private final boolean enabled;
    private final String apiKey;

    public GeminiProvider(
            @Value("${app.ai.gemini.api-key:}") String apiKey,
            @Value("${app.ai.gemini.model:gemini-1.5-flash}") String model,
            @Value("${app.ai.gemini.enabled:false}") boolean enabled,
            PromptTemplateService promptTemplateService,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = enabled && !apiKey.isBlank();
        this.promptTemplateService = promptTemplateService;
        this.objectMapper = objectMapper;

        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public AiGenerationResult generateExplanation(String problemTitle, String difficulty,
                                                    String code, String language, String tags) {
        String prompt = promptTemplateService.buildExplanationPrompt(
                problemTitle, difficulty, code, language, tags);

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    )),
                    "generationConfig", Map.of(
                            "temperature", 0.3,
                            "responseMimeType", "application/json"
                    )
            );

            Map<?, ?> response = webClient.post()
                    .uri("/models/{model}:generateContent?key={key}", model, apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseGeminiResponse(response);

        } catch (WebClientResponseException e) {
            log.error("Gemini API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiGenerationException("Gemini API error: " + e.getMessage(),
                    "AI_GENERATION_FAILED", e);
        }
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }

    private AiGenerationResult parseGeminiResponse(Map<?, ?> response) {
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            String text = (String) firstPart.get("text");

            // Count tokens from usageMetadata
            Map<?, ?> usageMeta = (Map<?, ?>) response.get("usageMetadata");
            int totalTokens = usageMeta != null
                    ? ((Number) usageMeta.get("totalTokenCount")).intValue() : 0;

            JsonNode jsonNode = objectMapper.readTree(text);
            String patternsJson = jsonNode.has("patterns") ? jsonNode.get("patterns").toString() : null;
            int tokenCount = totalTokens;

            return new AiGenerationResult(
                    jsonNode.path("problemSummary").asText(null),
                    jsonNode.path("bruteForceApproach").asText(null),
                    jsonNode.path("optimizedApproach").asText(null),
                    jsonNode.path("timeComplexity").asText(null),
                    jsonNode.path("spaceComplexity").asText(null),
                    patternsJson,
                    jsonNode.path("visuals").asText(null),
                    jsonNode.path("interviewNotes").asText(null),
                    jsonNode.path("commonMistakes").asText(null),
                    jsonNode.path("revisionNotes").asText(null),
                    "gemini-2.0-flash",
                    tokenCount
            );
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage(), e);
            throw new AiGenerationException("Failed to parse Gemini response", "AI_GENERATION_FAILED", e);
        }
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull()) ? child.asText() : null;
    }
}
