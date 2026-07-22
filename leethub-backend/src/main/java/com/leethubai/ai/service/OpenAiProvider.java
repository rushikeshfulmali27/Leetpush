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
 * OpenAI Chat Completions API provider.
 * Uses gpt-4o-mini by default for cost efficiency.
 */
@Slf4j
@Service
public class OpenAiProvider implements AiProvider {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final PromptTemplateService promptTemplateService;
    private final String model;
    private final boolean enabled;

    public OpenAiProvider(
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.model:gpt-4o-mini}") String model,
            @Value("${app.ai.openai.enabled:false}") boolean enabled,
            PromptTemplateService promptTemplateService,
            ObjectMapper objectMapper) {
        this.model = model;
        this.enabled = enabled && !apiKey.isBlank();
        this.promptTemplateService = promptTemplateService;
        this.objectMapper = objectMapper;

        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
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
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system",
                                    "content", "You are an expert software engineer and coding mentor. Always respond with valid JSON only."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.3,
                    "response_format", Map.of("type", "json_object")
            );

            Map<?, ?> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseOpenAiResponse(response);

        } catch (WebClientResponseException e) {
            log.error("OpenAI API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiGenerationException("OpenAI API error: " + e.getMessage(),
                    "AI_GENERATION_FAILED", e);
        }
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }

    private AiGenerationResult parseOpenAiResponse(Map<?, ?> response) {
        try {
            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
            String content = (String) message.get("content");

            // Parse token usage
            Map<?, ?> usage = (Map<?, ?>) response.get("usage");
            int totalTokens = usage != null ? ((Number) usage.get("total_tokens")).intValue() : 0;

            JsonNode json = objectMapper.readTree(content);
            return new AiGenerationResult(
                    getTextOrNull(json, "problemSummary"),
                    getTextOrNull(json, "bruteForceApproach"),
                    getTextOrNull(json, "optimizedApproach"),
                    getTextOrNull(json, "timeComplexity"),
                    getTextOrNull(json, "spaceComplexity"),
                    json.has("patterns") ? json.get("patterns").toString() : null,
                    getTextOrNull(json, "visuals"),
                    getTextOrNull(json, "interviewNotes"),
                    getTextOrNull(json, "commonMistakes"),
                    getTextOrNull(json, "revisionNotes"),
                    model,
                    totalTokens
            );
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", e.getMessage(), e);
            throw new AiGenerationException("Failed to parse OpenAI response", "AI_GENERATION_FAILED", e);
        }
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && !child.isNull()) ? child.asText() : null;
    }
}
