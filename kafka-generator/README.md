# Kafka Generator

## Description

This application is a Kafka generator service that published a random notification event to a Kafka topic.
Random mean userId, content and message type were define 4 to 5 content and 3 message type based on it generate and produce a message.

## Features

1. generate random message and produce to a kafka notification events topic.
2. producer never crash
3. Configurable initial delay and fixed delay for generating notifications and producer message to topic.
4. writing unit and integration testing for unit we are using mockito and for integration using testcontainer to isolate a test in docker.

## Architecture Overview

- Spring Boot as the microservice framework
- Kafka as the message broker
- Unit testing with Mockito
- Integration testing with Testcontainers

## Testing Strategy

- Unit Tests:
  - For each class, write unit tests to test the class methods.
  - Mock external dependencies using Mockito.
  - Test the critical business logic.
- Integration Tests:
  - Test the interaction between the application and the external dependencies.
  - Use Testcontainers to simulate the Kafka broker and the external services.
  - Test the integration between the application and the external services.

## Failure Handling Approach

- Producer Never Crash:
  - Handle exceptions and don't let the producer crash.
  - Implement retry mechanism for failed messages.
- Configurable Initial Delay and Fixed Delay:
  - Allow configuration of initial delay and fixed delay.
  - Use a scheduled task to generate notifications and produce messages.
- Writing Unit and Integration Testing:
  - Write unit tests to test the class methods.

## Trade-Offs

- Testing Time:
  - Writing unit tests takes less time compared to writing integration tests.
- Testing Complexity:
  - Integration tests are more complex compared to unit tests.
- Testing Difficulty:
  - Integration tests are more difficult to write and maintain compared to unit tests.
- Testing Coverage:
  - Unit tests cover the class methods, while integration tests cover the interaction between the application and external dependencies.