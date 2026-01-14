package com.impact.kafkagenerator;

import com.impact.kafkagenerator.payload.NotificationEvent;
import com.impact.kafkagenerator.service.impl.NotificationEventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private NotificationEventServiceImpl notificationEventService;
    private NotificationEvent notificationEvent;

    private static final String TOPIC = "notification-events";

    @BeforeEach
    void setupBefore() {
        ReflectionTestUtils.setField(notificationEventService, "topic", TOPIC);
        notificationEvent = NotificationEvent.builder()
                .userId("123")
                .messageType("EMAIL")
                .content("Account verification.")
                .build();
    }

    @Test
    void shouldSendNotificationEventSuccess() {
        notificationEventService.sendNotificationEvent(notificationEvent);
        verify(kafkaTemplate, times(1)).send(TOPIC, "123", notificationEvent);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    void shouldNotThrowExceptionWhenKafkaFails() {
        doThrow(new RuntimeException("Kafka is down."))
                .when(kafkaTemplate)
                .send(anyString(), anyString(), any());

        assertDoesNotThrow(() -> notificationEventService.sendNotificationEvent(notificationEvent));
        verify(kafkaTemplate).send(any(), any(), any());
    }
}
