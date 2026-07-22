package com.leethubai.search.controller;

import com.leethubai.search.service.SearchService;
import com.leethubai.common.dto.ValidationDTOs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidationDTOs.ApiResponse<Object>> search(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Searching: q={}, difficulty={}, tags={}, page={}, size={}", q, difficulty, tags, page, size);
        var results = searchService.search(q, difficulty, tags, page, size);
        return ResponseEntity.ok(ValidationDTOs.ApiResponse.success(results, "Search results retrieved"));
    }
}
