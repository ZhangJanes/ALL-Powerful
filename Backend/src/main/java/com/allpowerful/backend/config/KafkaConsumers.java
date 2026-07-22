package com.allpowerful.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaConsumers {
    @KafkaListener(topics = KafkaTopics.PHOTO_UPLOADED, groupId = "all-powerful-dev")
    public void onPhotoUploaded(String payload) {
        log.info("Consumed photo.uploaded event: {}", payload);
    }

    @KafkaListener(topics = KafkaTopics.MEMO_REMINDER_CHANGED, groupId = "all-powerful-dev")
    public void onMemoChanged(String payload) {
        log.info("Consumed memo.reminder.changed event: {}", payload);
    }
}
