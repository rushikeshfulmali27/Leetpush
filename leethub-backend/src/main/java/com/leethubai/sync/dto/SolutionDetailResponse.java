package com.leethubai.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolutionDetailResponse {
    private Long id;
    private String title;
    private String titleSlug;
    private String difficulty;
    private String language;
    private String code;
    private Integer runtimeMs;
    private String runtimePercentile;
    private Integer memoryKb;
    private String memoryPercentile;
    private String commitSha;
    private String githubUrl;
    private String syncStatus;
    private Instant submittedAt;
    private Instant syncedAt;
    private List<String> tags;
}
