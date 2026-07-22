package com.leethubai.notes.controller;

import com.leethubai.notes.service.NotesService;
import com.leethubai.common.dto.ValidationDTOs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Validated
public class NotesController {

    private final NotesService notesService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> createNote(
            @Valid @RequestBody CreateNoteRequest request) {
        log.info("Creating note for solution: {}", request.solutionId);
        var note = notesService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ValidationDTOs.ApiResponse.success(note, "Note created"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> listNotes(
            @RequestParam(required = false) Long solutionId,
            @RequestParam(required = false) String noteType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Listing notes: solutionId={}, noteType={}", solutionId, noteType);
        var notes = notesService.listNotes(solutionId, noteType, page, size);
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(notes, "Notes list retrieved"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> updateNote(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody UpdateNoteRequest request) {
        log.info("Updating note: id={}", id);
        var note = notesService.updateNote(id, request);
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(note, "Note updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNote(@PathVariable @NotNull Long id) {
        log.info("Deleting note: id={}", id);
        notesService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @Data
    @AllArgsConstructor
    public static class CreateNoteRequest {
        @NotNull
        private Long solutionId;

        @NotBlank
        private String content;

        private String noteType;
    }

    @Data
    @AllArgsConstructor
    public static class UpdateNoteRequest {
        @NotBlank
        private String content;

        private String noteType;
    }
}
