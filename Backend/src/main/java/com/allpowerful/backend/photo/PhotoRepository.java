package com.allpowerful.backend.photo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByUserIdAndLibraryIdOrderByCreatedAtDesc(Long userId, Long libraryId);
    List<Photo> findByUserIdAndLibraryIdInOrderByCreatedAtDesc(Long userId, List<Long> libraryIds);
}
