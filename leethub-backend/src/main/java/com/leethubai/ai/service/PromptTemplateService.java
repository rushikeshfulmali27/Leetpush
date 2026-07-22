package com.leethubai.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Loads prompt templates from classpath resources/prompts/ and fills in placeholders.
 */
@Slf4j
@Service
public class PromptTemplateService {

    private static final String PROMPTS_DIR = "prompts/";

    /**
     * Loads and fills the explanation prompt template.
     */
    public String buildExplanationPrompt(String title, String difficulty, String code,
                                          String language, String tags) {
        String template = loadTemplate("explanation_prompt.txt");
        return fillTemplate(template, Map.of(
                "title", title,
                "difficulty", difficulty,
                "code", code,
                "language", language,
                "tags", tags != null ? tags : ""
        ));
    }

    /**
     * Loads and fills the pattern recognition prompt template.
     */
    public String buildPatternPrompt(String title, String code, String language, String tags) {
        String template = loadTemplate("pattern_prompt.txt");
        return fillTemplate(template, Map.of(
                "title", title,
                "code", code,
                "language", language,
                "tags", tags != null ? tags : ""
        ));
    }

    /**
     * Loads and fills the revision notes prompt template.
     */
    public String buildRevisionPrompt(String title, String difficulty, String tags) {
        String template = loadTemplate("revision_prompt.txt");
        return fillTemplate(template, Map.of(
                "title", title,
                "difficulty", difficulty,
                "tags", tags != null ? tags : ""
        ));
    }

    private String loadTemplate(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(PROMPTS_DIR + fileName);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load prompt template: {}", fileName, e);
            throw new RuntimeException("Failed to load prompt template: " + fileName, e);
        }
    }

    private String fillTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
