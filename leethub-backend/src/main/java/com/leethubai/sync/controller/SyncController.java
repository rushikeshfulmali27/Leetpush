package com.leethubai.sync.controller;

import com.leethubai.sync.service.SyncService;
import com.leethubai.common.dto.ValidationDTOs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Slf4j
@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Validated
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<SyncSubmitResponse>> submitSolution(
            @Valid @RequestBody ValidationDTOs.SubmitSolutionRequest request) {
        log.info("Submitting solution for sync: {}", request.getTitle());
        var syncResponse = syncService.submitSolution(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ValidationDTOs.ApiResponse.success(syncResponse, "Solution queued for sync"));
    }

    @GetMapping("/status/{syncId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<ValidationDTOs.SyncStatusResponse>> getSyncStatus(
            @PathVariable @NotBlank String syncId) {
        log.info("Checking sync status: {}", syncId);
        var status = syncService.getSyncStatus(syncId);
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(status, "Sync status retrieved"));
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> getSyncHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        log.info("Retrieving sync history: page={}, size={}, status={}", page, size, status);
        var history = syncService.getSyncHistory(page, size, status);
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(history, "Sync history retrieved"));
    }

    public static class SyncSubmitResponse {
        public String syncId;
        public String status;
        public String message;

        public SyncSubmitResponse(String syncId, String status, String message) {
            this.syncId = syncId;
            this.status = status;
            this.message = message;
        }
    }
}
