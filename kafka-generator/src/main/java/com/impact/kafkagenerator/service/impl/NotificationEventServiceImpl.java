package com.impact.kafkagenerator.service.impl;

import com.impact.kafkagenerator.payload.NotificationEvent;
import com.impact.kafkagenerator.service.NotificationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventServiceImpl implements NotificationEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${notification.kafka.topic}")
    private String topic;

    @Override
    public void sendNotificationEvent(NotificationEvent notificationEvent) {
        try {
            kafkaTemplate.send(topic, notificationEvent.getUserId(), notificationEvent);
            log.info("Successfully send notification event for userId={}", notificationEvent.getUserId());
        } catch (Exception ex) {
            log.error("Exception occurred while sending notification event: {}", ex.getMessage(), ex);
        }
    }
}
