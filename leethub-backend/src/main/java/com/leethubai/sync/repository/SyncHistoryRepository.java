package com.leethubai.sync.repository;

import com.leethubai.sync.model.SyncHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncHistoryRepository extends JpaRepository<SyncHistory, Long> {

    List<SyncHistory> findBySolutionIdOrderByCreatedAtDesc(Long solutionId);
    
    @Query(value = "SELECT h FROM SyncHistory h JOIN FETCH h.solution WHERE h.solution.user.id = :userId",
           countQuery = "SELECT COUNT(h) FROM SyncHistory h WHERE h.solution.user.id = :userId")
    Page<SyncHistory> findBySolutionUserId(@Param("userId") Long userId, Pageable pageable);
}
