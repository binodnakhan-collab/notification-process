package com.impact.notificationconsumer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.impact.notificationconsumer.config.TestContainersConfig;
import com.impact.notificationconsumer.entity.UserEntity;
import com.impact.notificationconsumer.external.UserExternalCommunication;
import com.impact.notificationconsumer.payload.request.NotificationEvent;
import com.impact.notificationconsumer.payload.response.ExternalUserResponse;
import com.impact.notificationconsumer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${kafka.bootstrap-servers}"
})
@Import(TestContainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDatabaseTest {

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
    void setUp() throws InterruptedException {
        userRepository.deleteAll();
        Thread.sleep(2000);
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
        when(userExternalCommunication.getUserDetail(anyLong()))
                .thenReturn(
                        ExternalUserResponse.builder()
                                .userId(100L)
                                .firstName("Binod")
                                .lastName("Nakhan")
                                .email("binod11@yopmail.com")
                                .country("Nepal")
                                .address("Bhaktapur")
                                .build()
                );
        NotificationEvent event = new NotificationEvent(
                "100",
                "SMS",
                "Payment receipt"
        );
        kafkaTemplate.send(TOPIC, objectMapper.writeValueAsString(event));
        kafkaTemplate.flush();
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .pollDelay(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    List<UserEntity> users = userRepository.findAll();
                    assertThat(users).hasSize(1);
                });
        verify(userExternalCommunication, times(1)).getUserDetail(100L);


    }

}
