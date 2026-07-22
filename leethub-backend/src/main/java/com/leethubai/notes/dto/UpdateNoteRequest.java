package com.leethubai.notes.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class UpdateNoteRequest {
    private String content;
    private String noteType;
    private Instant reminderAt;
}
