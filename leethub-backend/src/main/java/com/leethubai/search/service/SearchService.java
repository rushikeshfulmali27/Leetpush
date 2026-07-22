package com.leethubai.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final com.leethubai.sync.repository.SolutionRepository solutionRepository;

    public Object search(String q, String difficulty, String tags, int page, int size) {
        log.info("Searching: q={}, difficulty={}, tags={}", q, difficulty, tags);
        
        com.leethubai.common.security.UserPrincipal principal = 
            (com.leethubai.common.security.UserPrincipal) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = principal.getId();

        org.springframework.data.jpa.domain.Specification<com.leethubai.sync.model.Solution> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (q != null && !q.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + q.trim().toLowerCase() + "%"));
            }
            if (difficulty != null && !difficulty.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("difficulty"), com.leethubai.sync.model.Difficulty.valueOf(difficulty.toUpperCase())));
            }
            if (tags != null && !tags.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("language"), tags)); // Using language for "tags" parameter if language parameter was meant
            }
            // Add a language check if it's passed via tags or if we just want to match it.
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "submittedAt"));
            
        org.springframework.data.domain.Page<com.leethubai.sync.model.Solution> solutions = solutionRepository.findAll(spec, pageable);

        java.util.List<java.util.Map<String, Object>> content = solutions.stream().map(s -> {
            java.util.Map<String, Object> hit = new java.util.HashMap<>();
            hit.put("id", s.getId());
            hit.put("title", s.getTitle());
            hit.put("difficulty", s.getDifficulty().name());
            hit.put("language", s.getLanguage());
            hit.put("syncStatus", s.getSyncStatus().name());
            hit.put("submittedAt", s.getSubmittedAt());
            hit.put("tags", java.util.List.of()); // Tags not currently stored on Solution
            return hit;
        }).toList();

        return java.util.Map.of(
            "content", content,
            "page", solutions.getNumber(),
            "size", solutions.getSize(),
            "totalElements", solutions.getTotalElements(),
            "totalPages", solutions.getTotalPages(),
            "query", q != null ? q : ""
        );
    }
}
