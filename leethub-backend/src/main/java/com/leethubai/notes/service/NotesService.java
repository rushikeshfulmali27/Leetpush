package com.leethubai.notes.service;

import com.leethubai.notes.controller.NotesController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotesService {

    public Object createNote(NotesController.CreateNoteRequest request) {
        log.info("Creating note for solution: {}", request.getSolutionId());
        return java.util.Map.of();
    }

    public Object listNotes(Long solutionId, String noteType, int page, int size) {
        log.info("Listing notes: solutionId={}, noteType={}", solutionId, noteType);
        return java.util.Map.of(
            "content", java.util.List.of(),
            "page", page,
            "size", size,
            "totalElements", 0,
            "totalPages", 0
        );
    }

    public Object updateNote(Long id, NotesController.UpdateNoteRequest request) {
        log.info("Updating note: id={}", id);
        return java.util.Map.of();
    }

    public void deleteNote(Long id) {
        log.info("Deleting note: id={}", id);
        // TODO: Implement note deletion
    }
}
