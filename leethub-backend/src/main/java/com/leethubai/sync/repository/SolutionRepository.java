package com.leethubai.sync.repository;

import com.leethubai.sync.model.Difficulty;
import com.leethubai.sync.model.Solution;
import com.leethubai.sync.model.SyncStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, Long>, JpaSpecificationExecutor<Solution> {

    Page<Solution> findByUserIdOrderBySubmittedAtDesc(Long userId, Pageable pageable);

    Page<Solution> findByUserIdAndSyncStatusOrderBySubmittedAtDesc(
            Long userId, SyncStatus syncStatus, Pageable pageable);

    Optional<Solution> findByUserIdAndLeetcodeIdAndLanguage(
            Long userId, String leetcodeId, String language);

    long countByUserId(Long userId);

    long countByUserIdAndDifficulty(Long userId, Difficulty difficulty);

    /** Returns distinct submission dates for a user in a given year (for heatmap). */
    @Query("SELECT FUNCTION('DATE', s.submittedAt) FROM Solution s " +
           "WHERE s.user.id = :userId " +
           "AND s.submittedAt >= :yearStart AND s.submittedAt <= :yearEnd " +
           "AND s.syncStatus = 'SYNCED'")
    List<Object> findSubmissionDatesByYear(
            @Param("userId") Long userId,
            @Param("yearStart") Instant yearStart,
            @Param("yearEnd") Instant yearEnd);

    /** All submission dates for streak calculation. */
    @Query("SELECT DISTINCT FUNCTION('DATE', s.submittedAt) FROM Solution s " +
           "WHERE s.user.id = :userId AND s.syncStatus = 'SYNCED' " +
           "ORDER BY 1 DESC")
    List<Object> findDistinctSubmissionDates(@Param("userId") Long userId);

    /** Language distribution: returns [language, count] pairs. */
    @Query("SELECT s.language, COUNT(s) FROM Solution s " +
           "WHERE s.user.id = :userId AND s.syncStatus = 'SYNCED' " +
           "GROUP BY s.language ORDER BY COUNT(s) DESC")
    List<Object[]> findLanguageDistribution(@Param("userId") Long userId);

    /** Count solutions submitted this week. */
    @Query("SELECT COUNT(s) FROM Solution s " +
           "WHERE s.user.id = :userId AND s.submittedAt >= :weekStart AND s.syncStatus = 'SYNCED'")
    long countByUserIdAndSubmittedAtAfter(
            @Param("userId") Long userId, @Param("weekStart") Instant weekStart);
}
