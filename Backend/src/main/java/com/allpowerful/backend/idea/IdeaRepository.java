package com.allpowerful.backend.idea;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
    List<Idea> findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(Long userId);
    List<Idea> findByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(Long userId);
    Optional<Idea> findByIdAndUserId(Long id, Long userId);
}
