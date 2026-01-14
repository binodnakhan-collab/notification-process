package com.impact.kafkagenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KafkaGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaGeneratorApplication.class, args);
    }

}
