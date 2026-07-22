package com.leethubai.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyncHistoryResponse {
    private Long id;           // Solution ID — used by frontend to navigate to /problems/:id
    private String syncId;     // e.g. "sync_123"
    private String title;
    private String difficulty;
    private String language;
    private String status;
    private Instant submittedAt;
    private Instant syncedAt;
    private String githubUrl;
    private Boolean hasAiExplanation;
}
