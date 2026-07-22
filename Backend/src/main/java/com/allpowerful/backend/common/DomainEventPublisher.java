package com.allpowerful.backend.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public DomainEventPublisher(@Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String payload) {
        if (kafkaTemplate != null) {
            kafkaTemplate.send(topic, payload);
        }
    }
}
