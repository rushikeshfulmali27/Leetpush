package com.leethubai.ai.service;

import com.leethubai.common.exception.AiGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Factory that selects the appropriate AI provider with automatic fallback.
 * Primary: OpenAI → Fallback: Gemini
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiProviderFactory {

    private final OpenAiProvider openAiProvider;
    private final GeminiProvider geminiProvider;

    /**
     * Generates an explanation using the primary provider (OpenAI) with automatic
     * fallback to Gemini if OpenAI is unavailable or throws an error.
     */
    public AiProvider.AiGenerationResult generateWithFallback(
            String title, String difficulty, String code, String language, String tags) {

        List<AiProvider> providers = getOrderedProviders();

        if (providers.isEmpty()) {
            throw new AiGenerationException(
                    "No AI providers are configured or enabled. Set app.ai.openai.enabled=true or app.ai.gemini.enabled=true",
                    "AI_GENERATION_FAILED");
        }

        Exception lastException = null;

        for (AiProvider provider : providers) {
            try {
                log.info("Attempting AI generation with provider: {}", provider.getProviderName());
                AiProvider.AiGenerationResult result =
                        provider.generateExplanation(title, difficulty, code, language, tags);
                log.info("AI generation succeeded with provider: {}", provider.getProviderName());
                return result;
            } catch (Exception e) {
                log.warn("AI provider {} failed: {}. Trying fallback...",
                        provider.getProviderName(), e.getMessage());
                lastException = e;
            }
        }

        throw new AiGenerationException(
                "All AI providers failed. Last error: " + lastException.getMessage(),
                "AI_GENERATION_FAILED", lastException);
    }

    /**
     * Returns available providers in priority order.
     */
    private List<AiProvider> getOrderedProviders() {
        return List.of(openAiProvider, geminiProvider).stream()
                .filter(AiProvider::isAvailable)
                .toList();
    }

    /**
     * Returns true if at least one provider is available.
     */
    public boolean hasAvailableProvider() {
        return openAiProvider.isAvailable() || geminiProvider.isAvailable();
    }
}
