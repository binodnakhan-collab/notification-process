package com.impact.kafkagenerator.integration;

import com.impact.kafkagenerator.config.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.kafka.KafkaContainer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = {TestContainersConfig.class})
class KafkaIntegrationTest {

    @Autowired
    KafkaContainer kafkaContainer;

    @Test
    void shouldStartKafka() {
        assertThat(kafkaContainer.isRunning()).isTrue();
    }

}
