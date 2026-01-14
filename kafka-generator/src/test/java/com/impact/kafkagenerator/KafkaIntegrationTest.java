package com.impact.kafkagenerator;

import com.impact.kafkagenerator.config.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.kafka.KafkaContainer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = {TestContainersConfig.class})
class KafkaIntegrationTest {

    @Autowired
    KafkaContainer kafkaContainer;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldStartKafka() {
        assertThat(kafkaContainer.isRunning()).isTrue();
    }

}
