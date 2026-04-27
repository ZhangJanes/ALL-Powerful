package com.allpowerful.backend.common;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String topic, String payload) {
        kafkaTemplate.send(topic, payload);
    }
}
