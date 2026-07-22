package com.leethubai.ai.repository;

import com.leethubai.ai.model.AiExplanation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiExplanationRepository extends JpaRepository<AiExplanation, Long> {

    Optional<AiExplanation> findBySolutionId(Long solutionId);

    boolean existsBySolutionId(Long solutionId);
}
