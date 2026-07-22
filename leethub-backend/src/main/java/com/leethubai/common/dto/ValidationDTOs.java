package com.leethubai.common.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ValidationDTOs {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;
        private long timestamp;

        public static <T> ApiResponse<T> success(T data, String message) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .data(data)
                    .message(message)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ApiError {
        private int status;
        private String error;
        private String code;
        private String message;
        private String path;
        private String traceId;
        private long timestamp;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubmitSolutionRequest {
        @NotBlank(message = "LeetCode ID cannot be blank")
        private String leetcodeId;

        @NotBlank(message = "Title cannot be blank")
        @Size(min = 1, max = 300, message = "Title must be between 1 and 300 characters")
        private String title;

        @NotBlank(message = "Title slug cannot be blank")
        private String titleSlug;

        @NotNull(message = "Difficulty cannot be null")
        @Pattern(regexp = "EASY|MEDIUM|HARD", message = "Difficulty must be EASY, MEDIUM, or HARD")
        private String difficulty;

        @NotBlank(message = "Language cannot be blank")
        private String language;

        @NotBlank(message = "Code cannot be blank")
        @Size(min = 1, max = 50000, message = "Code must be between 1 and 50000 characters")
        private String code;

        private String problemDescription;

        @NotNull(message = "Tags cannot be null")
        private java.util.List<String> tags;

        private Integer runtimeMs;
        private String runtimePercentile;
        private Integer memoryKb;
        private String memoryPercentile;

        @NotNull(message = "Submitted at cannot be null")
        private java.time.LocalDateTime submittedAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SyncStatusResponse {
        private String syncId;
        private String status;
        private String commitSha;
        private String githubUrl;
        private boolean aiGenerated;
        private java.time.LocalDateTime syncedAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SyncHistoryResponse {
        private String id;
        private String solutionId;
        private String title;
        private String difficulty;
        private String language;
        private String status;
        private String errorMessage;
        private java.time.LocalDateTime syncedAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GitHubOAuthCallbackRequest {
        @NotBlank(message = "OAuth code cannot be blank")
        private String code;

        @NotBlank(message = "State cannot be blank")
        private String state;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private long expiresIn;
        private UserResponse user;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String avatarUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token cannot be blank")
        private String refreshToken;
    }
}
