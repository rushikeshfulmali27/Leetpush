package com.leethubai.repo.repository;

import com.leethubai.repo.model.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    List<Repository> findByUserIdOrderByRepoNameAsc(Long userId);

    Optional<Repository> findByUserIdAndIsActiveTrue(Long userId);

    Optional<Repository> findByUserIdAndRepoFullName(Long userId, String repoFullName);

    @Modifying
    @Query("UPDATE Repository r SET r.isActive = false WHERE r.user.id = :userId")
    void deactivateAllForUser(Long userId);

    boolean existsByUserIdAndGithubRepoId(Long userId, String githubRepoId);
}
