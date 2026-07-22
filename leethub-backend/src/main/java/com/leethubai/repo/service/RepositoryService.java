package com.leethubai.repo.service;

import com.leethubai.auth.model.User;
import com.leethubai.auth.repository.UserRepository;
import com.leethubai.common.exception.ResourceNotFoundException;
import com.leethubai.repo.dto.RepositoryResponse;
import com.leethubai.repo.dto.SelectRepoRequest;
import com.leethubai.repo.model.Repository;
import com.leethubai.repo.repository.RepositoryRepository;
import com.leethubai.sync.repository.SolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import com.leethubai.common.security.EncryptionService;
import com.leethubai.sync.service.GitHubSyncService;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final UserRepository userRepository;
    private final SolutionRepository solutionRepository;
    private final GitHubSyncService gitHubSyncService;
    private final EncryptionService encryptionService;

    public List<RepositoryResponse> listRepositories(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 1. Fetch repositories from GitHub
        String token = encryptionService.decrypt(user.getGithubAccessToken());
        log.info("Fetching GitHub repos for user: {}", user.getUsername());
        List<Map<String, Object>> gitHubRepos = gitHubSyncService.getUserRepositories(token);
        log.info("Found {} GitHub repos", gitHubRepos != null ? gitHubRepos.size() : "null");

        // 2. Fetch local active repository mapping
        Repository activeRepo = repositoryRepository.findByUserIdAndIsActiveTrue(userId).orElse(null);
        String activeRepoFullName = activeRepo != null ? activeRepo.getRepoFullName() : null;

        // 3. Map GitHub repos to RepositoryResponse
        List<RepositoryResponse> responses = new ArrayList<>();
        if (gitHubRepos != null) {
            for (Map<String, Object> ghRepo : gitHubRepos) {
                String fullName = (String) ghRepo.get("full_name");
                String name = (String) ghRepo.get("name");
                String defaultBranch = (String) ghRepo.get("default_branch");
                boolean isActive = fullName.equals(activeRepoFullName);
                
                Long solutionCount = isActive ? solutionRepository.countByUserId(userId) : 0L;

                responses.add(RepositoryResponse.builder()
                        .repoName(name)
                        .repoFullName(fullName)
                        .defaultBranch(defaultBranch)
                        .isActive(isActive)
                        .solutionCount(solutionCount)
                        .build());
            }
        }
        return responses;
    }

    @Transactional
    public RepositoryResponse selectRepository(Long userId, SelectRepoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Find or create repository record
        Repository repo = repositoryRepository
                .findByUserIdAndRepoFullName(userId, request.getRepoFullName())
                .orElseGet(() -> {
                    // Extract repo name from full name (e.g. "johndoe/LeetCode" → "LeetCode")
                    String repoName = request.getRepoFullName().contains("/")
                            ? request.getRepoFullName().split("/")[1]
                            : request.getRepoFullName();

                    return Repository.builder()
                            .user(user)
                            .githubRepoId(request.getRepoFullName()) // use full name as stable ID
                            .repoName(repoName)
                            .repoFullName(request.getRepoFullName())
                            .defaultBranch(request.getBranch())
                            .build();
                });

        // Deactivate all repos for this user, then activate selected one
        repositoryRepository.deactivateAllForUser(userId);
        repo.setIsActive(true);
        repo.setDefaultBranch(request.getBranch());
        repo = repositoryRepository.save(repo);

        log.info("User {} selected repository {}", userId, repo.getRepoFullName());
        return mapToResponse(repo, null);
    }

    public RepositoryResponse getActiveRepository(Long userId) {
        Repository repo = repositoryRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ActiveRepository", "userId", userId));

        long solutionCount = solutionRepository.countByUserId(userId);
        return mapToResponse(repo, solutionCount);
    }

    private RepositoryResponse mapToResponse(Repository repo, Long solutionCount) {
        return RepositoryResponse.builder()
                .id(repo.getId())
                .repoName(repo.getRepoName())
                .repoFullName(repo.getRepoFullName())
                .defaultBranch(repo.getDefaultBranch())
                .isActive(repo.getIsActive())
                .solutionCount(solutionCount)
                .build();
    }
}
