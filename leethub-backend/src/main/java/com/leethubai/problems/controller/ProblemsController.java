package com.leethubai.problems.controller;

import com.leethubai.problems.service.ProblemsService;
import com.leethubai.common.dto.ValidationDTOs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
@Validated
public class ProblemsController {

    private final ProblemsService problemsService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> listProblems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "submittedAt") String sort) {
        log.info("Listing problems: page={}, size={}, difficulty={}, language={}", page, size, difficulty, language);
        var problems = problemsService.listProblems(page, size, difficulty, language, tags, sort);
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(problems, "Problems list retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> getProblemDetail(
            @PathVariable @NotNull Long id) {
        log.info("Retrieving problem detail: id={}", id);
        var problem = problemsService.getProblemDetail(id);
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(problem, "Problem detail retrieved"));
    }
}
