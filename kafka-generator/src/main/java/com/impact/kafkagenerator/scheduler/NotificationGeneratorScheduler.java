package com.impact.kafkagenerator.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.impact.kafkagenerator.payload.NotificationEvent;
import com.impact.kafkagenerator.service.NotificationEventService;
import com.impact.kafkagenerator.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationGeneratorScheduler {

    private final NotificationEventService notificationEventService;
    private final ObjectMapper objectMapper;

    @Scheduled(
            initialDelayString = "${notification.generator.initial-delay}",
            fixedDelayString = "${notification.generator.fixed-delay}"
    )
    public void generateNotificationEvent() {
        try {
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .userId(Helper.generateUserId())
                    .messageType(Helper.getRandomMessageType())
                    .content(Helper.getRandomContent())
                    .build();
            log.info("Generated notification event: {}", objectMapper.writeValueAsString(notificationEvent));
            notificationEventService.sendNotificationEvent(notificationEvent);
        } catch (Exception ex) {
            log.error("Error occurred while generating notification event: {}", ex.getMessage(), ex);
        }
    }
}
