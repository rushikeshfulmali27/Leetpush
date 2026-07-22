package com.leethubai.sync.repository;

import com.leethubai.sync.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    @Query(value = "SELECT t.name FROM tags t " +
                   "INNER JOIN solution_tags st ON t.id = st.tag_id " +
                   "WHERE st.solution_id = :solutionId", nativeQuery = true)
    List<String> findTagNamesBySolutionId(@Param("solutionId") Long solutionId);
}
