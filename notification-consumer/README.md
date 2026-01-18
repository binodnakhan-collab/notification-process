# Notification Consumer

This is a microservice that consumes notifications from a Kafka topic and performs necessary operations based on the received notifications.

## Features

1. Notification Consumption: The service consumes notifications from a Kafka topic named notification-events.
2. User Service Integration: The service integrates with the User Service to perform operations related to users. combined a data from external service and notification event and save to a database.
3. Notification Acknowledgement: The service sends acknowledgement messages to the notification producer to indicate that a notification has been processed successfully.
4. Handle all consumer issue, external service user query were skip and proper loggin.

## Getting Started
1. Prerequisites: Ensure that you have Docker and Docker Compose installed on your machine. with mysql 8 and kafka.