package com.leethubai.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
public class GitHubOAuthService {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;

    public GitHubOAuthService(
            @Value("${app.github.client-id}") String clientId,
            @Value("${app.github.client-secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.webClient = WebClient.builder()
                .baseUrl("https://github.com")
                .build();
    }

    /**
     * Exchange authorization code for access token.
     */
    public String exchangeCodeForToken(String code) {
        Map<String, String> response = webClient.post()
                .uri("/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("Failed to exchange GitHub authorization code");
        }

        return response.get("access_token");
    }

    /**
     * Fetch authenticated user's profile from GitHub API.
     */
    public GitHubUserInfo fetchUserInfo(String accessToken) {
        WebClient apiClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();

        Map<String, Object> userMap = apiClient.get()
                .uri("/user")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (userMap == null) {
            throw new RuntimeException("Failed to fetch GitHub user profile");
        }

        return new GitHubUserInfo(
                String.valueOf(userMap.get("id")),
                (String) userMap.get("login"),
                (String) userMap.get("email"),
                (String) userMap.get("avatar_url"),
                accessToken
        );
    }

    public record GitHubUserInfo(
            String id,
            String login,
            String email,
            String avatarUrl,
            String accessToken
    ) {}
}
