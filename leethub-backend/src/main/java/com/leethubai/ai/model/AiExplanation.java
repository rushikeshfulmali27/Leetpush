package com.leethubai.ai.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;


@Entity
@Table(name = "ai_explanations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiExplanation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solution_id", unique = true, nullable = false)
    private Long solutionId;

    @Column(name = "problem_summary", columnDefinition = "TEXT")
    private String problemSummary;

    @Column(name = "brute_force_approach", columnDefinition = "TEXT")
    private String bruteForceApproach;

    @Column(name = "optimized_approach", columnDefinition = "TEXT")
    private String optimizedApproach;

    @Column(name = "time_complexity", length = 50)
    private String timeComplexity;

    @Column(name = "space_complexity", length = 50)
    private String spaceComplexity;

    @Column(columnDefinition = "JSON")
    private String patterns;

    @Column(name = "visuals", columnDefinition = "TEXT")
    private String visuals;

    @Column(name = "interview_notes", columnDefinition = "TEXT")
    private String interviewNotes;

    @Column(name = "common_mistakes", columnDefinition = "TEXT")
    private String commonMistakes;

    @Column(name = "revision_notes", columnDefinition = "TEXT")
    private String revisionNotes;

    @Column(name = "ai_provider", length = 20)
    private String aiProvider;

    @Column(name = "ai_model", length = 50)
    private String aiModel;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "generated_at")
    private Instant generatedAt;

    /**
     * Parses the JSON patterns string into a List of strings.
     * Returns empty list if patterns is null or malformed.
     */
    public List<String> getPatternsList() {
        if (patterns == null || patterns.isBlank()) return Collections.emptyList();
        try {
            // patterns is stored as JSON array string e.g. ["Hash Map","Two Pointers"]
            String cleaned = patterns.replaceAll("[\\[\\]\"]", "");
            if (cleaned.isBlank()) return Collections.emptyList();
            return List.of(cleaned.split(",\\s*"));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

