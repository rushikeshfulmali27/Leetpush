package com.leethubai.auth.controller;

import com.leethubai.auth.dto.UserResponse;
import com.leethubai.auth.model.User;
import com.leethubai.auth.repository.UserRepository;
import com.leethubai.common.dto.ApiResponse;
import com.leethubai.common.exception.ResourceNotFoundException;
import com.leethubai.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /** GET /api/v1/users/me — Return the currently authenticated user */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        return ResponseEntity.ok(ApiResponse.success(toResponse(user)));
    }

    /** PATCH /api/v1/users/me — Update preferred language */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateUserRequest request) {

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        if (request.getPreferredLanguage() != null && !request.getPreferredLanguage().isBlank()) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }

        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(toResponse(user)));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .githubId(user.getGithubId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .preferredLanguage(user.getPreferredLanguage())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /** Inline DTO for PATCH body */
    @lombok.Data
    static class UpdateUserRequest {
        private String preferredLanguage;
    }
}
