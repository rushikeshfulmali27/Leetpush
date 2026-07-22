package com.leethubai.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiExplanationResponse {
    private Long solutionId;
    private String problemSummary;
    private String bruteForceApproach;
    private String optimizedApproach;
    private String timeComplexity;
    private String spaceComplexity;
    private List<String> patterns;
    private String interviewNotes;
    private String commonMistakes;
    private String revisionNotes;
    private String aiProvider;
    private String aiModel;
    private Instant generatedAt;
}
