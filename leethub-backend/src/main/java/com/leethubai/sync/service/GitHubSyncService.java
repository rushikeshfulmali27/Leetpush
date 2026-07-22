package com.leethubai.sync.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Handles GitHub API interactions for file creation/update.
 * Uses GitHub Contents API (REST v3) for individual files
 * and Git Trees API for batch commits.
 */
@Slf4j
@Service
public class GitHubSyncService {

    private final WebClient webClient;

    public GitHubSyncService(@Value("${app.github.api-url:https://api.github.com}") String apiUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    /**
     * Creates or updates a single file in a GitHub repository.
     *
     * @param repoFullName e.g. "johndoe/LeetCode"
     * @param path         e.g. "Arrays/Two_Sum/solution.java"
     * @param content      file contents (will be Base64 encoded)
     * @param commitMessage git commit message
     * @param branch       e.g. "main"
     * @param token        decrypted GitHub access token
     * @return commit SHA of the resulting commit
     */
    public String createOrUpdateFile(String repoFullName, String path, String content,
                                      String commitMessage, String branch, String token) {
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes());

        // Check if file exists to get its SHA (needed for updates)
        String existingSha = getFileSha(repoFullName, path, branch, token);

        Map<String, Object> body = existingSha != null
                ? Map.of("message", commitMessage, "content", encodedContent,
                          "branch", branch, "sha", existingSha)
                : Map.of("message", commitMessage, "content", encodedContent, "branch", branch);

        try {
            Map<?, ?> response = webClient.put()
                    .uri("/repos/{owner}/{repo}/contents/{path}",
                            getOwner(repoFullName), getRepoName(repoFullName), path)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("commit") instanceof Map<?, ?> commit) {
                String sha = (String) commit.get("sha");
                log.info("File committed to {}/{}: SHA={}", repoFullName, path, sha);
                return sha;
            }
        } catch (WebClientResponseException e) {
            log.error("GitHub API error creating file {}/{}: {} {}", repoFullName, path,
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("GitHub API error: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Commits multiple files in a single atomic batch commit using the Git Trees API.
     *
     * @param repoFullName  e.g. "johndoe/LeetCode"
     * @param files         list of {path, content} maps
     * @param commitMessage git commit message
     * @param branch        e.g. "main"
     * @param token         decrypted GitHub access token
     * @return commit SHA
     */
    public String batchCommit(String repoFullName, List<Map<String, String>> files,
                               String commitMessage, String branch, String token) {
        String owner = getOwner(repoFullName);
        String repo = getRepoName(repoFullName);

        try {
            // 1. Get latest commit SHA on branch
            Map<?, ?> refResponse = webClient.get()
                    .uri("/repos/{owner}/{repo}/git/ref/heads/{branch}", owner, repo, branch)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<?, ?> refObject = (Map<?, ?>) refResponse.get("object");
            String latestCommitSha = (String) refObject.get("sha");

            // 2. Get tree SHA of latest commit
            Map<?, ?> commitResponse = webClient.get()
                    .uri("/repos/{owner}/{repo}/git/commits/{sha}", owner, repo, latestCommitSha)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<?, ?> tree = (Map<?, ?>) commitResponse.get("tree");
            String baseTreeSha = (String) tree.get("sha");

            // 3. Create new tree with all files
            List<Map<String, String>> treeItems = files.stream()
                    .map(f -> Map.of(
                            "path", f.get("path"),
                            "mode", "100644",
                            "type", "blob",
                            "content", f.get("content")
                    ))
                    .toList();

            Map<?, ?> newTree = webClient.post()
                    .uri("/repos/{owner}/{repo}/git/trees", owner, repo)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("base_tree", baseTreeSha, "tree", treeItems))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String newTreeSha = (String) newTree.get("sha");

            // 4. Create commit
            Map<?, ?> newCommit = webClient.post()
                    .uri("/repos/{owner}/{repo}/git/commits", owner, repo)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "message", commitMessage,
                            "tree", newTreeSha,
                            "parents", List.of(latestCommitSha)
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String newCommitSha = (String) newCommit.get("sha");

            // 5. Update branch ref to point to new commit
            webClient.patch()
                    .uri("/repos/{owner}/{repo}/git/refs/heads/{branch}", owner, repo, branch)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("sha", newCommitSha))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Batch committed {} files to {}: SHA={}", files.size(), repoFullName, newCommitSha);
            return newCommitSha;

        } catch (WebClientResponseException e) {
            log.error("GitHub batch commit failed for {}: {} {}", repoFullName,
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("GitHub batch commit failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches the SHA of an existing file (needed for updates).
     * Returns null if the file does not exist yet.
     */
    public String getFileSha(String repoFullName, String path, String branch, String token) {
        try {
            Map<?, ?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/contents/{path}")
                            .queryParam("ref", branch)
                            .build(getOwner(repoFullName), getRepoName(repoFullName), path))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response != null ? (String) response.get("sha") : null;
        } catch (WebClientResponseException.NotFound e) {
            return null; // file doesn't exist yet — this is fine for new files
        } catch (WebClientResponseException e) {
            log.warn("Could not get SHA for {}/{}: {}", repoFullName, path, e.getMessage());
            return null;
        }
    }

    /**
     * Verifies the repository exists and is accessible with the given token.
     */
    public boolean verifyRepositoryAccess(String repoFullName, String token) {
        try {
            webClient.get()
                    .uri("/repos/{owner}/{repo}", getOwner(repoFullName), getRepoName(repoFullName))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return true;
        } catch (WebClientResponseException e) {
            log.warn("Repository access check failed for {}: {}", repoFullName, e.getStatusCode());
            return false;
        }
    }

    public List<Map<String, Object>> getUserRepositories(String token) {
        try {
            return webClient.get()
                    .uri("/user/repos?sort=updated&per_page=100")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .map(map -> (Map<String, Object>) map)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch user repositories: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        }
    }

    private String getOwner(String repoFullName) {
        return repoFullName.split("/")[0];
    }

    private String getRepoName(String repoFullName) {
        return repoFullName.split("/")[1];
    }
}
