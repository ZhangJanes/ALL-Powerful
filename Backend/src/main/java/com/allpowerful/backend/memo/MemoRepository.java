package com.allpowerful.backend.memo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    List<Memo> findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(Long userId);
    List<Memo> findByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(Long userId);
}
