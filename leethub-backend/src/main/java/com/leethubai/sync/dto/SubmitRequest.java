package com.leethubai.sync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class SubmitRequest {

    @NotBlank(message = "LeetCode ID is required")
    private String leetcodeId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Title slug is required")
    private String titleSlug;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    private List<String> tags;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Code is required")
    private String code;

    private Integer runtimeMs;
    private String runtimePercentile;
    private Integer memoryKb;
    private String memoryPercentile;

    @NotNull(message = "Submission time is required")
    private Instant submittedAt;
}
