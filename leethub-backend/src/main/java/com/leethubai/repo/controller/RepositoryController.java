package com.leethubai.repo.controller;

import com.leethubai.common.dto.ApiResponse;
import com.leethubai.common.security.UserPrincipal;
import com.leethubai.repo.dto.RepositoryResponse;
import com.leethubai.repo.dto.SelectRepoRequest;
import com.leethubai.repo.service.RepositoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    /** GET /api/v1/repositories — List all repositories for the authenticated user */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<RepositoryResponse>>>> listRepositories(
            @AuthenticationPrincipal UserPrincipal principal) {
        System.out.println("====== REPOSITORY CONTROLLER HIT ======");
        List<RepositoryResponse> repos = repositoryService.listRepositories(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("repositories", repos)));
    }

    /** POST /api/v1/repositories/select — Select a target repository */
    @PostMapping("/select")
    public ResponseEntity<ApiResponse<RepositoryResponse>> selectRepository(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SelectRepoRequest request) {

        RepositoryResponse repo = repositoryService.selectRepository(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Repository selected successfully", repo));
    }

    /** GET /api/v1/repositories/active — Get the currently active repository */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<RepositoryResponse>> getActiveRepository(
            @AuthenticationPrincipal UserPrincipal principal) {

        RepositoryResponse repo = repositoryService.getActiveRepository(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(repo));
    }
}
