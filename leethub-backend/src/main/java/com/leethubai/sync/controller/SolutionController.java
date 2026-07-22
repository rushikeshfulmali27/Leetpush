package com.leethubai.sync.controller;

import com.leethubai.common.dto.ApiResponse;
import com.leethubai.common.exception.ResourceNotFoundException;
import com.leethubai.common.security.UserPrincipal;
import com.leethubai.sync.dto.SolutionDetailResponse;
import com.leethubai.sync.model.Solution;
import com.leethubai.sync.repository.SolutionRepository;
import com.leethubai.sync.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solutions")
@RequiredArgsConstructor
public class SolutionController {

    private final SolutionRepository solutionRepository;
    private final TagService tagService;

    /** GET /api/v1/solutions/{id} — Return a single solution with its tags */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SolutionDetailResponse>> getSolution(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        Solution solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution", "id", id));

        // Ownership check
        if (!solution.getUser().getId().equals(principal.getId())) {
            throw new ResourceNotFoundException("Solution", "id", id);
        }

        List<String> tags = tagService.getTagsForSolution(id);

        return ResponseEntity.ok(ApiResponse.success(toDetailResponse(solution, tags)));
    }

    private SolutionDetailResponse toDetailResponse(Solution s, List<String> tags) {
        return SolutionDetailResponse.builder()
                .id(s.getId())
                .title(s.getTitle())
                .titleSlug(s.getTitleSlug())
                .difficulty(s.getDifficulty() != null ? s.getDifficulty().name() : null)
                .language(s.getLanguage())
                .code(s.getCode())
                .runtimeMs(s.getRuntimeMs())
                .runtimePercentile(s.getRuntimePercentile())
                .memoryKb(s.getMemoryKb())
                .memoryPercentile(s.getMemoryPercentile())
                .commitSha(s.getCommitSha())
                .githubUrl(s.getGithubPath())
                .syncStatus(s.getSyncStatus().name())
                .submittedAt(s.getSubmittedAt())
                .syncedAt(s.getSyncedAt())
                .tags(tags)
                .build();
    }
}
