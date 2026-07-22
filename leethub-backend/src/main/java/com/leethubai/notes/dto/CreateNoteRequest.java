package com.leethubai.notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateNoteRequest {

    @NotNull(message = "solutionId is required")
    private Long solutionId;

    @NotBlank(message = "content is required")
    private String content;

    private String noteType = "PERSONAL";

    private Instant reminderAt;
}
