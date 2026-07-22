package com.leethubai.sync.service;

import com.leethubai.sync.model.Tag;
import com.leethubai.sync.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages tags and the solution_tags join table.
 * Uses a find-or-create strategy: if a tag name already exists, reuse it;
 * otherwise insert a new row. Tags are stored case-insensitively.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Persist all tags for a solution, creating new tag rows as needed.
     * Idempotent — re-inserting the same (solutionId, tagId) pair is a no-op.
     */
    @Transactional
    public void saveTagsForSolution(Long solutionId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;

        for (String name : tagNames) {
            if (name == null || name.isBlank()) continue;
            String trimmed = name.trim();

            // Find-or-create
            Tag tag = tagRepository.findByNameIgnoreCase(trimmed)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder().name(trimmed).category("algorithm").build();
                        return tagRepository.save(newTag);
                    });

            // Insert into join table (ignore duplicates)
            jdbcTemplate.update(
                    "INSERT IGNORE INTO solution_tags (solution_id, tag_id) VALUES (?, ?)",
                    solutionId, tag.getId()
            );
        }

        log.debug("Saved {} tags for solution {}", tagNames.size(), solutionId);
    }

    /** Returns the tag names for a given solution. */
    public List<String> getTagsForSolution(Long solutionId) {
        return tagRepository.findTagNamesBySolutionId(solutionId);
    }
}
