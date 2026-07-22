package com.leethubai.auth.service;

import com.leethubai.auth.model.User;
import com.leethubai.auth.repository.UserRepository;
import com.leethubai.common.dto.ValidationDTOs;
import com.leethubai.common.security.JwtTokenProvider;
import com.leethubai.common.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.github.client-id}")
    private String clientId;

    @Value("${app.github.client-secret}")
    private String clientSecret;

    @Value("${app.github.api-url}")
    private String githubApiUrl;

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EncryptionService encryptionService;
    private final RestTemplate restTemplate = new RestTemplate();

    public AuthService.AuthUrlResponse generateGitHubAuthUrl() {
        String state = UUID.randomUUID().toString();
        String authUrl = String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&scope=repo,user:email&state=%s",
                clientId, state
        );
        return new AuthUrlResponse(authUrl, state);
    }

    public ValidationDTOs.TokenResponse handleGitHubCallback(String code, String state) {
        log.info("Processing real GitHub OAuth callback with state: {}", state);

        // 1. Exchange code for access token
        String tokenUrl = "https://github.com/login/oauth/access_token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, request, Map.class);
        Map<String, Object> tokenBody = tokenResponse.getBody();
        log.info("Token body from GitHub: {}", tokenBody);
        
        if (tokenBody == null || !tokenBody.containsKey("access_token")) {
            throw new RuntimeException("Failed to retrieve access token from GitHub: " + tokenBody);
        }
        
        String githubAccessToken = (String) tokenBody.get("access_token");

        // 2. Fetch user profile from GitHub
        HttpHeaders apiHeaders = new HttpHeaders();
        apiHeaders.setBearerAuth(githubAccessToken);
        apiHeaders.set(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");
        
        HttpEntity<Void> apiRequest = new HttpEntity<>(apiHeaders);
        ResponseEntity<Map> userResponse = restTemplate.exchange(
                githubApiUrl + "/user", HttpMethod.GET, apiRequest, Map.class);
                
        Map<String, Object> userBody = userResponse.getBody();
        if (userBody == null || !userBody.containsKey("id")) {
            throw new RuntimeException("Failed to retrieve user profile from GitHub");
        }

        String githubId = String.valueOf(userBody.get("id"));
        String login = (String) userBody.get("login");
        String email = (String) userBody.get("email");
        String avatarUrl = (String) userBody.get("avatar_url");

        // 3. Find or create user
        User user = userRepository.findByGithubId(githubId).orElseGet(() -> {
            User newUser = User.builder()
                    .githubId(githubId)
                    .username(login)
                    .email(email)
                    .avatarUrl(avatarUrl)
                    .githubAccessToken(encryptionService.encrypt(githubAccessToken)) // Store it initially
                    .build();
            return userRepository.save(newUser);
        });

        // Always update the githubAccessToken in case it changed
        user.setGithubAccessToken(encryptionService.encrypt(githubAccessToken));
        user = userRepository.save(user);

        // 4. Generate real JWT tokens
        String jwtAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getGithubId());
        String jwtRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        var userDto = ValidationDTOs.UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return ValidationDTOs.TokenResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(userDto)
                .build();
    }

    public AuthService.TokenRefreshResponse refreshAccessToken(String refreshToken) {
        log.info("Refreshing access token");
        // Verify refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getGithubId());
        return new TokenRefreshResponse(newAccessToken, jwtTokenProvider.getAccessTokenExpirationMs() / 1000);
    }

    // Keep the nested response classes
    public static class AuthUrlResponse {
        public String authUrl;
        public String state;

        public AuthUrlResponse(String authUrl, String state) {
            this.authUrl = authUrl;
            this.state = state;
        }
    }

    public static class TokenRefreshResponse {
        public String accessToken;
        public long expiresIn;

        public TokenRefreshResponse(String accessToken, long expiresIn) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
        }
    }
}
