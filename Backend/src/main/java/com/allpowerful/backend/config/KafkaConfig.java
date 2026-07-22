package com.allpowerful.backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaConfig {
    @Bean
    NewTopic photoUploadedTopic() {
        return TopicBuilder.name(KafkaTopics.PHOTO_UPLOADED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic memoReminderChangedTopic() {
        return TopicBuilder.name(KafkaTopics.MEMO_REMINDER_CHANGED).partitions(1).replicas(1).build();
    }
}
