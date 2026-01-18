# Kafka Generator

## Description

This application is a Kafka generator service that published a random notification event to a Kafka topic.
Random mean userId, content and message type were define 4 to 5 content and 3 message type based on it generate and produce a message.

## Features

- generate random message and produce to a kafka notification events topic.
- producer never crash
- Configurable initial delay and fixed delay for generating notifications and producer message to topic.
