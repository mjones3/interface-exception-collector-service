# Interface Exception Collector Service

Centralized collection and management of interface exceptions from all BioPro interface services.

## Overview

This service collects exception events from various interface services (Order, Collection, Distribution) via Kafka, stores them in a centralized database, and provides a REST API for the dashboard UI to display and manage these exceptions.

## Architecture

```
Interface Services → Kafka → Exception Collector → Database → Dashboard UI
                      ↑                ↓
                      └── retrieve original payload ──┘
```

## Features

- ✅ **Event Collection**: Listens to rejection events from all interface services
- ✅ **Centralized Storage**: Stores all exceptions in a single database
- ✅ **Original Payload Retrieval**: Fetches original customer requests for context
- ✅ **Retry Mechanism**: Enables reprocessing of failed requests
- ✅ **REST API**: Provides endpoints for dashboard UI consumption
- ✅ **Real-time Updates**: Immediate visibility into interface exceptions

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Kafka
- PostgreSQL

### Running Locally

```bash
# Start dependencies
docker-compose up -d

# Run the service
./mvnw spring-boot:run
```

### API Documentation

Once running, visit: http://localhost:8080/swagger-ui.html

## Event Types Supported

- `OrderRejected`
- `OrderCancelled`
- `CollectionRejected`
- `DistributionFailed`

## Configuration

See `src/main/resources/config/application.yml` for configuration options.
