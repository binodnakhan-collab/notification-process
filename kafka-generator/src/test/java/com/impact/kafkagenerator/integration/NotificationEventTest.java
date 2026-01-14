package com.impact.kafkagenerator.integration;

import com.impact.kafkagenerator.config.TestContainersConfig;
import com.impact.kafkagenerator.payload.NotificationEvent;
import com.impact.kafkagenerator.service.NotificationEventService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.kafka.KafkaContainer;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${kafka.bootstrap-servers}"
})
@Import(TestContainersConfig.class)
class NotificationEventTest {


    @Autowired
    private NotificationEventService notificationEventService;

    @Autowired
    private KafkaContainer kafkaContainer;

    private KafkaConsumer<String, NotificationEvent> consumer;

    private final String TOPIC = "notification-events";

    @BeforeEach
    void setUp() {
        String bootstrapServers = kafkaContainer.getBootstrapServers();
        System.out.println("Kafka Bootstrap Servers: " + bootstrapServers);
        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "testGroup",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        );

         consumer = new KafkaConsumer<>(consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(NotificationEvent.class, false)
        );

        consumer.subscribe(List.of(TOPIC));
    }

    @Test
    public void shouldSendNotificationEvent() {
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .messageType("SMS")
                .userId("123")
                .content("Welcome to our platform.")
                .build();

        notificationEventService.sendNotificationEvent(notificationEvent);
        ConsumerRecord<String, NotificationEvent> record =
                KafkaTestUtils.getSingleRecord(consumer, TOPIC);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(notificationEvent.getUserId());
        assertThat(record.value().getUserId()).isEqualTo(notificationEvent.getUserId());
        assertThat(record.value().getMessageType()).isEqualTo(notificationEvent.getMessageType());
    }

}
