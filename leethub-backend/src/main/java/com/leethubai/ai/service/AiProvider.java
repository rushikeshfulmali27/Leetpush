package com.leethubai.ai.service;

/**
 * Interface for AI content generation providers.
 * Implementations: OpenAiProvider, GeminiProvider
 */
public interface AiProvider {

    /**
     * Generate a complete explanation for a coding problem solution.
     *
     * @param problemTitle  the problem title
     * @param difficulty    EASY, MEDIUM, or HARD
     * @param code          the submitted code
     * @param language      the programming language
     * @param tags          problem tags (e.g., Array, Hash Table)
     * @return generated explanation text (structured)
     */
    AiGenerationResult generateExplanation(String problemTitle, String difficulty,
                                            String code, String language, String tags);

    /**
     * Get the provider name (e.g., "openai", "gemini").
     */
    String getProviderName();

    /**
     * Check if the provider is currently available.
     */
    boolean isAvailable();

    record AiGenerationResult(
            String problemSummary,
            String bruteForceApproach,
            String optimizedApproach,
            String timeComplexity,
            String spaceComplexity,
            String patterns,
            String visuals,
            String interviewNotes,
            String commonMistakes,
            String revisionNotes,
            String model,
            Integer tokenCount
    ) {}
}
