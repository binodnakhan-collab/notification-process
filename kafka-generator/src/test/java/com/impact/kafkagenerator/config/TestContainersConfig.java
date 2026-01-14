package com.impact.kafkagenerator.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainersConfig {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        DockerImageName kafkaImage = DockerImageName
                .parse("confluentinc/cp-kafka:7.3.2")
                .asCompatibleSubstituteFor("apache/kafka");
        KafkaContainer kafkaContainer = new KafkaContainer(kafkaImage)
                .withReuse(true);
        kafkaContainer.start();
        return kafkaContainer;
    }

}
