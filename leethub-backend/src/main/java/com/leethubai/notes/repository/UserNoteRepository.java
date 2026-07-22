package com.leethubai.notes.repository;

import com.leethubai.notes.model.NoteType;
import com.leethubai.notes.model.UserNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNoteRepository extends JpaRepository<UserNote, Long> {

    Page<UserNote> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<UserNote> findByUserIdAndSolutionIdOrderByCreatedAtDesc(
            Long userId, Long solutionId, Pageable pageable);

    Page<UserNote> findByUserIdAndNoteTypeOrderByCreatedAtDesc(
            Long userId, NoteType noteType, Pageable pageable);
}
