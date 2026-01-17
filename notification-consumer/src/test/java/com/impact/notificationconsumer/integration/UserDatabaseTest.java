package com.impact.notificationconsumer.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.impact.notificationconsumer.config.TestContainersConfig;
import com.impact.notificationconsumer.entity.UserEntity;
import com.impact.notificationconsumer.external.UserExternalCommunication;
import com.impact.notificationconsumer.payload.request.NotificationEvent;
import com.impact.notificationconsumer.payload.response.ExternalUserResponse;
import com.impact.notificationconsumer.repository.UserRepository;
import com.impact.notificationconsumer.service.UserService;
import com.impact.notificationconsumer.service.impl.UserServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestContainersConfig.class)
public class UserDatabaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private UserExternalCommunication userExternalCommunication;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOPIC = "notification-events";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveUserToDatabase() {
        UserEntity user = UserEntity.builder()
                .fullName("Binod Nakhan")
                .email("binod@yopmail.com")
                .country("Nepal")
                .doe(LocalDateTime.now())
                .address("Bhaktapur")
                .notificationEventContext("{\"userId\": 511, \"content\": \"Welcome to our service!\", \"messageType\": \"EMAIL\"}")
                .build();
        UserEntity newUser = userRepository.save(user);
        assertThat(newUser.getId()).isNotNull();
    }

    @Test
    void shouldConsumeNotificationEventAndPersistToDB() throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .userId("100")
                .messageType("SMS")
                .content("Hello, how are you?")
                .build();

        ExternalUserResponse externalUserResponse = ExternalUserResponse.builder()
                .userId(Long.valueOf(notificationEvent.getUserId()))
                .firstName("Binod")
                .lastName("Nakhan")
                .city("Bhaktapur")
                .email("binod@yopmail.com")
                .state("Bagmati")
                .phoneNumber("9766919210")
                .address("Bhaktapur")
                .zipCode("44800")
                .build();

        when(userExternalCommunication.getUserDetail(Long.valueOf(notificationEvent.getUserId()))).thenReturn(externalUserResponse);
        String message = objectMapper.writeValueAsString(notificationEvent);
        kafkaTemplate.send(TOPIC, message);

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<UserEntity> users = userRepository.findAll();
                    assertThat(users).hasSize(1);
                });

        verify(userExternalCommunication, times(1)).getUserDetail(Long.valueOf(notificationEvent.getUserId()));
        verify(userExternalCommunication, times(1))
                .getUserDetail(Long.valueOf(notificationEvent.getUserId()));
    }

}
