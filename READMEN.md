# Docker Compose Deployment

## Prerequisites

- Docker and Docker Compose installed on your machine

## How to run

1. Clone the repository
2. Navigate to the directory where the `docker-compose.yml` is located
3. run command docker compose up -d or docker compose up --build

## access rest api of consumer 
to access user list curl --location 'http://localhost:8000/users?pageNo=0&pageSize=10&sortBy=id&sortDirection=asc'
to access user detail curl --location 'http://localhost:8000/users/1'