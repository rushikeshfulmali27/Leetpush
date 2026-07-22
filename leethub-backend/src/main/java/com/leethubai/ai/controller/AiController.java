package com.leethubai.ai.controller;

import com.leethubai.ai.dto.AiExplanationResponse;
import com.leethubai.ai.model.AiExplanation;
import com.leethubai.ai.service.AiService;
import com.leethubai.common.dto.ApiResponse;
import com.leethubai.common.exception.ResourceNotFoundException;
import com.leethubai.common.security.UserPrincipal;
import com.leethubai.sync.model.Solution;
import com.leethubai.sync.repository.SolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final SolutionRepository solutionRepository;

    /** GET /api/v1/ai/explanations/{solutionId} — Get existing AI explanation */
    @GetMapping("/explanations/{solutionId}")
    public ResponseEntity<ApiResponse<AiExplanationResponse>> getExplanation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long solutionId) {

        // Verify solution belongs to the user
        Solution solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Solution", "id", solutionId));

        if (!solution.getUser().getId().equals(principal.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }

        AiExplanation explanation = aiService.getExplanation(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AiExplanation", "solutionId", solutionId));

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(explanation)));
    }

    /** POST /api/v1/ai/explanations/{solutionId}/regenerate — Force regenerate explanation */
    @PostMapping("/explanations/{solutionId}/regenerate")
    public ResponseEntity<ApiResponse<AiExplanationResponse>> regenerateExplanation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long solutionId) {

        Solution solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Solution", "id", solutionId));

        if (!solution.getUser().getId().equals(principal.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }

        AiExplanation explanation = aiService.generateAndSave(
                solutionId,
                solution.getTitle(),
                solution.getDifficulty().name(),
                solution.getCode(),
                solution.getLanguage(),
                "" // tags not stored on solution directly — will add in future
        );

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(explanation)));
    }

    private AiExplanationResponse mapToResponse(AiExplanation e) {
        return AiExplanationResponse.builder()
                .solutionId(e.getSolutionId())
                .problemSummary(e.getProblemSummary())
                .bruteForceApproach(e.getBruteForceApproach())
                .optimizedApproach(e.getOptimizedApproach())
                .timeComplexity(e.getTimeComplexity())
                .spaceComplexity(e.getSpaceComplexity())
                .patterns(e.getPatternsList())
                .interviewNotes(e.getInterviewNotes())
                .commonMistakes(e.getCommonMistakes())
                .revisionNotes(e.getRevisionNotes())
                .aiProvider(e.getAiProvider())
                .aiModel(e.getAiModel())
                .generatedAt(e.getGeneratedAt())
                .build();
    }
}
