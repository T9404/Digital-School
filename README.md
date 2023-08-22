# ⏩✌ Spring Boot Microservices Project 

This project demonstrates a microservices architecture using Spring Boot, along with various technologies for service discovery, communication, security, and more. The project consists of the following microservices:

- **auth-server**: Handles user authentication and authorization using JWT.
- **config-server**: Centralized configuration management for all microservices.
- **discovery**: Utilizes Eureka for service discovery and registration.
- **gateway**: Acts as an API gateway, routing requests to appropriate microservices.
- **notification**: Manages notification of temperature, wind speed to children and school staff
- **school**: Handles school-related operations.
- **student**: Manages student-related operations.

## Technologies Used

- **Spring Boot**: Framework for building microservices.
- **Eureka**: Service discovery and registration.
- **Zipkin**: Distributed tracing system for monitoring microservices interactions.
- **PostgresSQL**: Relational database used for data storage.
- **RabbitMQ**: Messaging broker for inter-microservice communication.
- **JWT**: JSON Web Tokens for secure authentication.
- **OpenAPI**: Used to document the REST APIs.

## Microservices Communication

The microservices communicate with each other using HTTP and RabbitMQ messaging. The `gateway` service routes external requests to appropriate microservices based on their paths.

## Configuration

- All microservices use the `config-server` to fetch their configurations. Configuration files are stored in a central repository.
- Modify the configuration files in the repository to suit your needs.

## Authentication and Security

- The `auth-server` manages user authentication and provides JWT tokens for secure communication between microservices.
- Email confirmation, password changes, mail changes occur through a confirmation code from the mail

## Weather Data

- The `school` and `student` services fetch weather data from the Open-Meteo API for display purposes.

## Monitoring

- Zipkin is used to trace requests and visualize the interactions between microservices.
