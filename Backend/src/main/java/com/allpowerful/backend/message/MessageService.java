package com.allpowerful.backend.message;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageQueryMapper messageQueryMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public List<MessageItem> list(Long userId) {
        String cacheKey = "msg:agg:" + userId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null && cached.equals("HOT")) {
            // 简化示例：仅演示缓存命中标记
        } else {
            stringRedisTemplate.opsForValue().set(cacheKey, "HOT", Duration.ofSeconds(30));
        }

        LocalDateTime now = LocalDateTime.now();
        return messageQueryMapper.listMessageRows(userId, now).stream()
                .map(r -> new MessageItem(
                        r.sourceType(),
                        r.sourceId(),
                        r.title(),
                        r.content(),
                        r.plannedAt(),
                        statusLabel(r.plannedAt(), now)
                ))
                .toList();
    }

    private String statusLabel(LocalDateTime plannedAt, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        long dayDiff = ChronoUnit.DAYS.between(today, plannedAt.toLocalDate());
        if (dayDiff < 0) return "过期";
        if (dayDiff == 0) return "今天";
        if (dayDiff == 1) return "明天";
        if (dayDiff == 2) return "后天";
        return "大后天";
    }
}
