package com.leethubai.ai.service;

import com.leethubai.ai.model.AiExplanation;
import com.leethubai.ai.repository.AiExplanationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiExplanationRepository aiExplanationRepository;
    private final AiProviderFactory providerFactory;

    /**
     * Returns an existing AI explanation for a solution (from DB cache).
     */
    public Optional<AiExplanation> getExplanation(Long solutionId) {
        return aiExplanationRepository.findBySolutionId(solutionId);
    }

    /**
     * Generates a new AI explanation for a solution and persists it.
     * Uses the provider factory with automatic OpenAI → Gemini fallback.
     */
    public AiExplanation generateAndSave(Long solutionId, String title, String difficulty,
                                          String code, String language, String tags) {
        log.info("Generating AI explanation for solution: {} ({})", solutionId, title);

        // Delete any existing explanation before regenerating
        aiExplanationRepository.findBySolutionId(solutionId)
                .ifPresent(existing -> aiExplanationRepository.delete(existing));

        AiProvider.AiGenerationResult result = providerFactory.generateWithFallback(
                title, difficulty, code, language, tags);

        AiExplanation explanation = AiExplanation.builder()
                .solutionId(solutionId)
                .problemSummary(result.problemSummary())
                .bruteForceApproach(result.bruteForceApproach())
                .optimizedApproach(result.optimizedApproach())
                .timeComplexity(result.timeComplexity())
                .spaceComplexity(result.spaceComplexity())
                .patterns(result.patterns())
                .visuals(result.visuals())
                .interviewNotes(result.interviewNotes())
                .commonMistakes(result.commonMistakes())
                .revisionNotes(result.revisionNotes())
                .aiModel(result.model())
                .tokenCount(result.tokenCount())
                .generatedAt(Instant.now())
                .build();

        // Set provider name based on model
        String providerName = result.model() != null && result.model().startsWith("gemini")
                ? "gemini" : "openai";
        explanation.setAiProvider(providerName);

        AiExplanation saved = aiExplanationRepository.save(explanation);
        log.info("AI explanation saved for solution {} using provider={}, model={}, tokens={}",
                solutionId, providerName, result.model(), result.tokenCount());
        return saved;
    }
}
