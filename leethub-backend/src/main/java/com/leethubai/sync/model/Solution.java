package com.leethubai.sync.model;

import com.leethubai.auth.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "solutions", indexes = {
        @Index(name = "idx_solutions_user_id", columnList = "user_id"),
        @Index(name = "idx_solutions_sync_status", columnList = "sync_status"),
        @Index(name = "idx_solutions_submitted_at", columnList = "user_id, submitted_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "idx_solutions_user_leetcode",
                columnNames = {"user_id", "leetcode_id", "language"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "repository_id")
    private Long repositoryId;

    @Column(name = "leetcode_id", nullable = false, length = 100)
    private String leetcodeId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "title_slug", nullable = false, length = 300)
    private String titleSlug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false, length = 30)
    private String language;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String code;

    @Column(name = "runtime_ms")
    private Integer runtimeMs;

    @Column(name = "runtime_percentile", length = 10)
    private String runtimePercentile;

    @Column(name = "memory_kb")
    private Integer memoryKb;

    @Column(name = "memory_percentile", length = 10)
    private String memoryPercentile;

    @Column(name = "commit_sha", length = 40)
    private String commitSha;

    @Column(name = "github_path", length = 500)
    private String githubPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "synced_at")
    private Instant syncedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
