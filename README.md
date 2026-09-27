# API Gateway - Architecture

## Overview

API Gateway is responsible for routing and managing incoming client requests across the Event Ticketing System microservices.

It acts as the centralized entry point for client applications and forwards requests to the appropriate backend services.

## Responsibilities

- Request Routing
- Centralized API Entry Point
- Service-to-Service Communication
- Request Management
- Environment-based Configuration
- Gateway-level Request Processing

## Tech Stack

| Technology | Version / Details |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.4 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Gateway | Gateway Starter |
| dotenv-java | 3.0.0 |
| Maven | Build Tool |
| Reactor Test | Testing |
| Spring Boot Test | Testing |

## Dependencies

The API Gateway uses the following main dependencies:

- `spring-cloud-starter-gateway`
- `dotenv-java`
- `spring-boot-starter-test`
- `reactor-test`

## Architecture

```mermaid
flowchart LR

    Client[Client / Frontend]

    Gateway[API Gateway]

    UserService[User Service]
    EventService[Event Service]
    BookingService[Booking Service]
    PaymentService[Payment Service]

    Client --> Gateway

    Gateway --> UserService
    Gateway --> EventService
    Gateway --> BookingService
    Gateway --> PaymentService
