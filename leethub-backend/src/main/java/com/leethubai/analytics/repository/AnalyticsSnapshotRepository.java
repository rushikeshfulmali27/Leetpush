package com.leethubai.analytics.repository;

import com.leethubai.analytics.model.AnalyticsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AnalyticsSnapshotRepository extends JpaRepository<AnalyticsSnapshot, Long> {

    Optional<AnalyticsSnapshot> findByUserIdAndSnapshotDate(Long userId, LocalDate date);

    Optional<AnalyticsSnapshot> findFirstByUserIdOrderBySnapshotDateDesc(Long userId);
}
