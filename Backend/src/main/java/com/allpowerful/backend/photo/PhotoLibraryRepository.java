package com.allpowerful.backend.photo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhotoLibraryRepository extends JpaRepository<PhotoLibrary, Long> {
    List<PhotoLibrary> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<PhotoLibrary> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}
