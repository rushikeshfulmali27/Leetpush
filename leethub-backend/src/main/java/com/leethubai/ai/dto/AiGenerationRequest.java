package com.leethubai.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiGenerationRequest {
    @NotNull(message = "solutionId is required")
    private Long solutionId;
    private String tags;
}
