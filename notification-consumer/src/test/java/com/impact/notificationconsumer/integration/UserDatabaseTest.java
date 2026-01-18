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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestContainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
        Mockito.reset(userExternalCommunication);

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
    void shouldProcessNotificationEventAndPersistUserSuccessfully() throws Exception {
        ExternalUserResponse userResponse = ExternalUserResponse.builder()
                .userId(100L)
                .firstName("Binod")
                .lastName("Nakhan")
                .email("binod1@yopmail.com")
                .country("Nepal")
                .address("Bhaktapur")
                .build();

        when(userExternalCommunication.getUserDetail(anyLong()))
                .thenReturn(userResponse);

        NotificationEvent randomEvent = new NotificationEvent(
                String.valueOf(ThreadLocalRandom.current().nextInt(1, 1000)),
                "PUSH_NOTIFICATION",
                "Payment receipt."
        );

        String message = objectMapper.writeValueAsString(randomEvent);
        kafkaTemplate.send(TOPIC, message);

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<UserEntity> users = userRepository.findAll();
                    assertFalse(users.isEmpty());
                });

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(userExternalCommunication, timeout(5000))
                .getUserDetail(captor.capture());
    }



}
