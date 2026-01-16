package com.impact.notificationconsumer.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.impact.notificationconsumer.payload.request.NotificationEvent;
import com.impact.notificationconsumer.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {

    private final ObjectMapper objectMapper;
    private final UserService userService;


    @KafkaListener(topics = "${notification.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeNotificationEvent(ConsumerRecord<String, String> message, Acknowledgment acknowledgment) {
        log.info("Received message topic={}, partition={}, offset={}", message.topic(), message.partition(), message.offset());
        try {
            NotificationEvent notificationEvent = objectMapper.readValue(message.value(), NotificationEvent.class);
            if (notificationEvent == null || notificationEvent.getUserId().isBlank() || notificationEvent.getContent().isBlank() || notificationEvent.getMessageType().isBlank()) {
                log.warn("Invalid message structured as some parameters are missing: {}", message.value());
                acknowledgment.acknowledge();
                return;
            }
            log.info("Processing notification event message -> userId={}, messageType={}, content={}", notificationEvent.getUserId(), notificationEvent.getMessageType(), notificationEvent.getContent());
            /*
            * process message and persist to database in user entity table.
            * */
            userService.processNotificationEvent(message.value());
            acknowledgment.acknowledge();
            log.info("committed successfully.");
        }catch (Exception ex) {
            log.error("Failed to process notification event message so skipping payload={}", message.value(), ex);
            acknowledgment.acknowledge();
        }
    }

}
