package com.leethubai.sync.service;

import com.leethubai.auth.model.User;
import com.leethubai.auth.repository.UserRepository;
import com.leethubai.common.dto.ValidationDTOs;
import com.leethubai.common.exception.ResourceNotFoundException;
import com.leethubai.common.security.EncryptionService;
import com.leethubai.common.security.UserPrincipal;
import com.leethubai.repo.model.Repository;
import com.leethubai.repo.repository.RepositoryRepository;
import com.leethubai.sync.controller.SyncController;
import com.leethubai.sync.model.Difficulty;
import com.leethubai.sync.model.Solution;
import com.leethubai.sync.model.SyncHistory;
import com.leethubai.sync.model.SyncStatus;
import com.leethubai.sync.repository.SolutionRepository;
import com.leethubai.sync.repository.SyncHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final SolutionRepository solutionRepository;
    private final SyncHistoryRepository syncHistoryRepository;
    private final UserRepository userRepository;
    private final RepositoryRepository repositoryRepository;
    private final GitHubSyncService gitHubSyncService;
    private final EncryptionService encryptionService;
    private final com.leethubai.ai.service.AiService aiService;

    @Transactional
    public SyncController.SyncSubmitResponse submitSolution(ValidationDTOs.SubmitSolutionRequest request) {
        log.info("Submitting solution: {}", request.getTitle());

        // 1. Get Current User
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId().toString()));

        // 2. Create or Update Solution Entity
        Solution solution = solutionRepository.findByUserIdAndLeetcodeIdAndLanguage(user.getId(), request.getLeetcodeId(), request.getLanguage())
                .orElseGet(() -> Solution.builder()
                        .user(user)
                        .leetcodeId(request.getLeetcodeId())
                        .language(request.getLanguage())
                        .build());

        boolean isNew = solution.getId() == null;

        solution.setTitle(request.getTitle());
        solution.setTitleSlug(request.getTitleSlug());
        solution.setDifficulty(Difficulty.valueOf(request.getDifficulty()));
        solution.setCode(request.getCode());
        solution.setRuntimeMs(request.getRuntimeMs());
        solution.setRuntimePercentile(request.getRuntimePercentile());
        solution.setMemoryKb(request.getMemoryKb());
        solution.setMemoryPercentile(request.getMemoryPercentile());
        solution.setSubmittedAt(request.getSubmittedAt().toInstant(ZoneOffset.UTC));
        solution.setSyncStatus(SyncStatus.PENDING); 
        Solution savedSolution = solutionRepository.save(solution);

        String syncMessage = "Solution queued for sync and AI generation in the background";
        
        repositoryRepository.findByUserIdAndIsActiveTrue(user.getId()).ifPresent(activeRepo -> {
            String token = encryptionService.decrypt(user.getGithubAccessToken());
            
            // Run async to avoid blocking the UI during AI generation
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    // 1. Generate AI Explanation
                    String tagsStr = request.getTags() != null ? String.join(", ", request.getTags()) : "";
                    com.leethubai.ai.model.AiExplanation aiExplanation = aiService.generateAndSave(
                            savedSolution.getId(), request.getTitle(), request.getDifficulty(),
                            request.getCode(), request.getLanguage(), tagsStr);

                    // 2. Build README.md
                    StringBuilder readme = new StringBuilder();
                    readme.append("# ").append(request.getTitle()).append("\n\n");
                    
                    if (request.getProblemDescription() != null && !request.getProblemDescription().isBlank()) {
                        readme.append("## Problem Statement\n");
                        readme.append(request.getProblemDescription()).append("\n\n");
                    }

                    readme.append("## Logic and Implementation\n");
                    if (aiExplanation.getOptimizedApproach() != null) {
                        readme.append(aiExplanation.getOptimizedApproach()).append("\n\n");
                    }
                    if (aiExplanation.getVisuals() != null && !aiExplanation.getVisuals().isBlank()) {
                        readme.append("### Visuals\n```mermaid\n");
                        readme.append(aiExplanation.getVisuals()).append("\n```\n\n");
                    }

                    readme.append("## Code\n```").append(request.getLanguage().toLowerCase()).append("\n");
                    readme.append(request.getCode()).append("\n```\n");

                    // 3. Push to GitHub
                    String ext = getExtensionForLanguage(request.getLanguage());
                    String basePath = String.format("%s/%s", request.getDifficulty(), request.getTitleSlug());
                    String codePath = String.format("%s/solution%s", basePath, ext);
                    String readmePath = String.format("%s/README.md", basePath);
                    String commitMessage = String.format("Sync LeetCode: %s [%s]", request.getTitle(), request.getDifficulty());

                    // Push code
                    String sha = gitHubSyncService.createOrUpdateFile(
                            activeRepo.getRepoFullName(), codePath, request.getCode(), commitMessage, activeRepo.getDefaultBranch(), token);

                    // Push README
                    gitHubSyncService.createOrUpdateFile(
                            activeRepo.getRepoFullName(), readmePath, readme.toString(), commitMessage + " (README)", activeRepo.getDefaultBranch(), token);

                    if (sha != null) {
                        Solution toUpdate = solutionRepository.findById(savedSolution.getId()).orElse(savedSolution);
                        toUpdate.setSyncStatus(SyncStatus.SYNCED);
                        toUpdate.setCommitSha(sha);
                        toUpdate.setGithubPath(codePath);
                        toUpdate.setSyncedAt(Instant.now());
                        toUpdate.setRepositoryId(activeRepo.getId());
                        solutionRepository.save(toUpdate);

                        syncHistoryRepository.save(SyncHistory.builder()
                                .solution(toUpdate)
                                .eventType(SyncHistory.EventType.SYNCED)
                                .status(SyncHistory.Status.SUCCESS)
                                .commitSha(sha)
                                .build());
                    }
                } catch (Exception e) {
                    log.error("Failed async sync for solution: {}", savedSolution.getId(), e);
                    Solution toUpdate = solutionRepository.findById(savedSolution.getId()).orElse(savedSolution);
                    toUpdate.setSyncStatus(SyncStatus.FAILED);
                    solutionRepository.save(toUpdate);
                    
                    syncHistoryRepository.save(SyncHistory.builder()
                            .solution(toUpdate)
                            .eventType(SyncHistory.EventType.SYNCED)
                            .status(SyncHistory.Status.FAILURE)
                            .errorMessage("Failed to generate AI or push to GitHub: " + e.getMessage())
                            .build());
                }
            });
        });

        // Initial history record for just the "CREATED" or "UPDATED" DB save
        SyncHistory history = SyncHistory.builder()
                .solution(savedSolution)
                .eventType(isNew ? SyncHistory.EventType.CREATED : SyncHistory.EventType.UPDATED)
                .status(SyncHistory.Status.SUCCESS)
                .build();
        syncHistoryRepository.save(history);

        String syncId = "sync_" + UUID.randomUUID().toString().substring(0, 8);
        return new SyncController.SyncSubmitResponse(syncId, savedSolution.getSyncStatus().name(), syncMessage);
    }

    public ValidationDTOs.SyncStatusResponse getSyncStatus(String syncId) {
        log.info("Getting sync status for: {}", syncId);
        return ValidationDTOs.SyncStatusResponse.builder()
                .syncId(syncId)
                .status("SUCCESS")
                .build();
    }

    @Transactional(readOnly = true)
    public Object getSyncHistory(int page, int size, String status) {
        log.info("Getting sync history: page={}, size={}, status={}", page, size, status);
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        org.springframework.data.domain.Page<SyncHistory> historyPage = syncHistoryRepository.findBySolutionUserId(principal.getId(), pageable);
        
        java.util.List<ValidationDTOs.SyncHistoryResponse> content = historyPage.stream().map(history -> {
            ValidationDTOs.SyncHistoryResponse dto = new ValidationDTOs.SyncHistoryResponse();
            dto.setId(history.getId().toString());
            dto.setSolutionId(history.getSolution().getId().toString());
            dto.setTitle(history.getSolution().getTitle());
            dto.setDifficulty(history.getSolution().getDifficulty().name());
            dto.setLanguage(history.getSolution().getLanguage());
            dto.setStatus(history.getStatus().name());
            dto.setErrorMessage(history.getErrorMessage());
            dto.setSyncedAt(history.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(history.getCreatedAt(), java.time.ZoneId.of("UTC")) : null);
            return dto;
        }).toList();

        return java.util.Map.of(
            "content", content,
            "page", historyPage.getNumber(),
            "size", historyPage.getSize(),
            "totalElements", historyPage.getTotalElements(),
            "totalPages", historyPage.getTotalPages()
        );
    }

    private String getExtensionForLanguage(String lang) {
        return switch (lang.toLowerCase()) {
            case "java" -> ".java";
            case "python", "python3" -> ".py";
            case "cpp", "c++" -> ".cpp";
            case "c" -> ".c";
            case "javascript" -> ".js";
            case "typescript" -> ".ts";
            case "go" -> ".go";
            case "rust" -> ".rs";
            case "ruby" -> ".rb";
            case "swift" -> ".swift";
            case "kotlin" -> ".kt";
            case "csharp", "c#" -> ".cs";
            case "php" -> ".php";
            default -> ".txt";
        };
    }
}
