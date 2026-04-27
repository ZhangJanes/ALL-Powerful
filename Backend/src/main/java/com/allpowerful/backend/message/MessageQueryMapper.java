package com.allpowerful.backend.message;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MessageQueryMapper {
    List<MessageRow> listMessageRows(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    record MessageRow(String sourceType, Long sourceId, String title, String content, LocalDateTime plannedAt) {}
}
