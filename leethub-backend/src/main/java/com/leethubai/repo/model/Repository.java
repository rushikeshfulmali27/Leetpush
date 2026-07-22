package com.leethubai.repo.model;

import com.leethubai.auth.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "repositories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_repo",
                columnNames = {"user_id", "github_repo_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Repository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "github_repo_id", nullable = false, length = 50)
    private String githubRepoId;

    @Column(name = "repo_name", nullable = false, length = 200)
    private String repoName;

    @Column(name = "repo_full_name", nullable = false, length = 300)
    private String repoFullName;

    @Column(name = "default_branch", length = 50)
    @Builder.Default
    private String defaultBranch = "main";

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = false;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
