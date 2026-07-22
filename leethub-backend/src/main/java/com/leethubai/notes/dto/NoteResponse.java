package com.leethubai.notes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoteResponse {
    private Long id;
    private Long solutionId;
    private String problemTitle;
    private String content;
    private String noteType;
    private Instant reminderAt;
    private Instant createdAt;
    private Instant updatedAt;
}
