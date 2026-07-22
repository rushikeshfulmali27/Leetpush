package com.leethubai.auth.controller;

import com.leethubai.auth.service.AuthService;
import com.leethubai.common.dto.ValidationDTOs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @GetMapping("/github/url")
    public ResponseEntity<AuthService.AuthUrlResponse> getGitHubAuthUrl() {
        log.info("Generating GitHub OAuth URL");
        var response = authService.generateGitHubAuthUrl();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/github/callback")
    public ResponseEntity<ValidationDTOs.ApiResponse<ValidationDTOs.TokenResponse>> handleGitHubCallback(
            @Valid @RequestBody ValidationDTOs.GitHubOAuthCallbackRequest request) {
        log.info("Processing GitHub OAuth callback");
        var tokenResponse = authService.handleGitHubCallback(request.getCode(), request.getState());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ValidationDTOs.ApiResponse.success(tokenResponse, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ValidationDTOs.ApiResponse<AuthService.TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody ValidationDTOs.RefreshTokenRequest request) {
        log.info("Refreshing access token");
        var response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(response, "Token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        log.info("User logged out");
        return ResponseEntity.noContent().build();
    }
}

