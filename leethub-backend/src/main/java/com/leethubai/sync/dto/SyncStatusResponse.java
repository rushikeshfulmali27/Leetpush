package com.leethubai.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncStatusResponse {

    private String syncId;
    private String status;
    private String commitSha;
    private String githubUrl;
    private boolean aiGenerated;
    private Instant syncedAt;
    private String message;
}
