package com.leethubai.sync.service;

import com.leethubai.ai.model.AiExplanation;
import com.leethubai.common.util.SlugUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Builds the file tree structure for GitHub commits.
 * Generates: solution.{ext}, README.md, notes.md
 */
@Slf4j
@Service
public class FileTreeBuilder {

    /**
     * Determines the GitHub path for a solution file.
     * e.g., "Arrays/Two_Sum/solution.java"
     */
    public String getFilePath(List<String> tags, String title, String language) {
        String category = resolveCategory(tags);
        String folder = SlugUtils.toFolderName(title);
        String extension = getFileExtension(language);
        return String.format("%s/%s/solution.%s", category, folder, extension);
    }

    /**
     * Returns the folder path (without filename).
     * e.g., "Arrays/Two_Sum"
     */
    public String getFolderPath(List<String> tags, String title) {
        String category = resolveCategory(tags);
        String folder = SlugUtils.toFolderName(title);
        return String.format("%s/%s", category, folder);
    }

    /**
     * Builds the README.md content for a solution.
     */
    public String buildReadme(String title, String difficulty, String language,
                               List<String> tags, Integer runtimeMs, String runtimePercentile,
                               Integer memoryKb, String memoryPercentile,
                               LocalDate solvedDate, AiExplanation aiExplanation) {

        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(title).append("\n\n");

        // Metadata badges
        sb.append("![Difficulty](https://img.shields.io/badge/Difficulty-")
                .append(difficulty).append("-")
                .append(getDifficultyColor(difficulty)).append(")\n");

        if (language != null) {
            sb.append("![Language](https://img.shields.io/badge/Language-")
                    .append(language).append("-blue)\n");
        }
        sb.append("\n");

        // Tags
        if (tags != null && !tags.isEmpty()) {
            sb.append("**Topics:** ").append(String.join(", ", tags)).append("\n\n");
        }

        // Performance
        if (runtimeMs != null) {
            sb.append("**Runtime:** ").append(runtimeMs).append(" ms");
            if (runtimePercentile != null) {
                sb.append(" (beats ").append(runtimePercentile).append(")");
            }
            sb.append("  \n");
        }
        if (memoryKb != null) {
            sb.append("**Memory:** ").append(String.format("%.1f", memoryKb / 1024.0)).append(" MB");
            if (memoryPercentile != null) {
                sb.append(" (beats ").append(memoryPercentile).append(")");
            }
            sb.append("\n\n");
        }

        if (solvedDate != null) {
            sb.append("**Solved:** ").append(solvedDate).append("\n\n");
        }

        sb.append("---\n\n");

        // AI Explanation
        if (aiExplanation != null) {
            sb.append("## Problem Summary\n\n");
            if (aiExplanation.getProblemSummary() != null) {
                sb.append(aiExplanation.getProblemSummary()).append("\n\n");
            }

            sb.append("## Approach\n\n");
            if (aiExplanation.getBruteForceApproach() != null) {
                sb.append("### Brute Force\n\n")
                        .append(aiExplanation.getBruteForceApproach()).append("\n\n");
            }
            if (aiExplanation.getOptimizedApproach() != null) {
                sb.append("### Optimized Solution\n\n")
                        .append(aiExplanation.getOptimizedApproach()).append("\n\n");
            }

            sb.append("## Complexity Analysis\n\n");
            if (aiExplanation.getTimeComplexity() != null) {
                sb.append("- **Time Complexity:** ").append(aiExplanation.getTimeComplexity()).append("\n");
            }
            if (aiExplanation.getSpaceComplexity() != null) {
                sb.append("- **Space Complexity:** ").append(aiExplanation.getSpaceComplexity()).append("\n");
            }
            sb.append("\n");

            if (aiExplanation.getPatternsList() != null && !aiExplanation.getPatternsList().isEmpty()) {
                sb.append("## Patterns & Techniques\n\n");
                for (String pattern : aiExplanation.getPatternsList()) {
                    sb.append("- ").append(pattern).append("\n");
                }
                sb.append("\n");
            }

            if (aiExplanation.getInterviewNotes() != null) {
                sb.append("## Interview Notes\n\n")
                        .append(aiExplanation.getInterviewNotes()).append("\n\n");
            }

            if (aiExplanation.getCommonMistakes() != null) {
                sb.append("## Common Mistakes\n\n")
                        .append(aiExplanation.getCommonMistakes()).append("\n\n");
            }

            if (aiExplanation.getRevisionNotes() != null) {
                sb.append("## Quick Revision\n\n")
                        .append(aiExplanation.getRevisionNotes()).append("\n\n");
            }
        }

        sb.append("---\n\n");
        sb.append("*Auto-generated by [LeetHub AI](https://leethub.ai)*\n");
        return sb.toString();
    }

    /**
     * Builds a README.md without AI explanation (placeholder).
     */
    public String buildReadmeWithoutAi(String title, String difficulty, String language,
                                        List<String> tags, Integer runtimeMs,
                                        String runtimePercentile, Integer memoryKb,
                                        String memoryPercentile, LocalDate solvedDate) {
        return buildReadme(title, difficulty, language, tags, runtimeMs, runtimePercentile,
                memoryKb, memoryPercentile, solvedDate, null);
    }

    /**
     * Builds a simple notes.md template for user annotations.
     */
    public String buildNotesTemplate(String title) {
        return "# Notes: " + title + "\n\n" +
                "## Key Insights\n\n_Write your key takeaways here..._\n\n" +
                "## Things to Remember\n\n_Add important patterns or edge cases..._\n\n" +
                "## Related Problems\n\n_List similar problems for review..._\n";
    }

    /** Maps language name to file extension. */
    public String getFileExtension(String language) {
        if (language == null) return "txt";
        return switch (language.toLowerCase()) {
            case "java" -> "java";
            case "python", "python3" -> "py";
            case "c++" -> "cpp";
            case "c" -> "c";
            case "c#" -> "cs";
            case "javascript" -> "js";
            case "typescript" -> "ts";
            case "go" -> "go";
            case "kotlin" -> "kt";
            case "rust" -> "rs";
            case "swift" -> "swift";
            case "ruby" -> "rb";
            case "scala" -> "scala";
            case "php" -> "php";
            case "r" -> "r";
            default -> language.toLowerCase();
        };
    }

    /** Picks primary category from tags list (first data structure or algorithm tag). */
    private String resolveCategory(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "Misc";
        // Priority categories
        List<String> priorityTags = List.of("Array", "String", "Hash Table", "Dynamic Programming",
                "Tree", "Graph", "Linked List", "Binary Search", "Stack", "Queue",
                "Heap", "Greedy", "Backtracking", "Bit Manipulation", "Math",
                "Sorting", "Sliding Window", "Two Pointers", "Recursion", "Design");
        for (String priority : priorityTags) {
            if (tags.contains(priority)) {
                return SlugUtils.toFolderName(priority);
            }
        }
        return SlugUtils.toFolderName(tags.get(0));
    }

    private String getDifficultyColor(String difficulty) {
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> "brightgreen";
            case "MEDIUM" -> "yellow";
            case "HARD" -> "red";
            default -> "lightgrey";
        };
    }
}
