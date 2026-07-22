package com.leethubai.problems.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemsService {

    public Object listProblems(int page, int size, String difficulty, String language, String tags, String sort) {
        log.info("Listing problems: page={}, size={}, difficulty={}, language={}", page, size, difficulty, language);
        return java.util.Map.of(
            "content", java.util.List.of(),
            "page", page,
            "size", size,
            "totalElements", 0,
            "totalPages", 0
        );
    }

    public Object getProblemDetail(Long id) {
        log.info("Getting problem detail: id={}", id);
        return java.util.Map.of();
    }
}
