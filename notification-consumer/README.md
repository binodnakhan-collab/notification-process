# Notification Consumer

This is a microservice that consumes notifications from a Kafka topic and performs necessary operations based on the received notifications.

## Features

1. Notification Consumption: The service consumes notifications from a Kafka topic named notification-events.
2. User Service Integration: The service integrates with the User Service to perform operations related to users. combined a data from external service and notification event and save to a database.
3. Notification Acknowledgement: The service sends acknowledgement messages to the notification producer to indicate that a notification has been processed successfully.
4. Handle all consumer issue, external service user query were skip and proper loggin.

## Getting Started
## Architecture Overview

The Notification Consumer service is built using Spring Boot and leverages Spring Cloud Stream Kafka for consuming notifications from a Kafka topic. It integrates with the User Service to retrieve user information. The service uses HikariCP for connection pooling and MySQL 8 as the database. 

## Testing Strategy

- Unit Tests:
    - For each class, write unit tests to test the class methods.
    - Mock external dependencies using Mockito.
    - Test the critical business logic.
- Integration Tests:
    - Test the interaction between the application and the external dependencies.
    - Use Testcontainers to simulate the Kafka broker and the external services.
    - Test the integration between the application and the external services.
## Failure-Handling Approach

The service uses weblclient retry mechanism when timeout case.

## Trade-Offs

The service makes a trade-off in terms of performance and latency. By integrating with the User Service, there is added latency in the processing of notifications. However, the service ensures data integrity by performing operations on the user information.

### Continuous Integration and Deployment

The service is continuously integrated and deployed using GitHub Actions. The service is built and tested using Docker containersß.
1. Prerequisites: Ensure that you have Docker and Docker Compose installed on your machine. with mysql 8 and kafka.ß