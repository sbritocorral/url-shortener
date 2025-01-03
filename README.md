# URL Shortener

URL shortener is a MVP that converts long URLs into shorter, easy-to-share links. It uses Kotlin, Spring Boot (WebFlux / Project Reactor), R2DBC for database access, and Flyway for migrations. It follows a layered architecture that separates entities, domain logic, and request/response DTOs.

## Uses
- Java 21
- Kotlin
- Gradle
- Postgres
- Flyway for migrations
- KtLint for linting
- Kover for test coverage
- GitHub Actions for CI
- Spring Boot Actuator for health checks

## API Overview

The service provides endpoints for:
- Creating shortened URLs (POST /api/v1/urls)
- Resolving short URLs (GET /api/v1/urls/{shortId})
- Health checks (GET /actuator/health)

Detailed API documentation is available through Swagger UI:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs


## Approach
- **Layered Design**: Entities for persistence, domain classes for business logic, and DTOs for API requests/responses.
- **Short ID Generation**: Uses a truncated SHA-256 hash (Base62-encoded) for shorter, more human-friendly IDs.
- **Hash Storage**: We store a full SHA-256 digest in url_hash (BYTEA) for duplicate detection. The URL is normalized to ensure consistent hashing.
- **Unique Constraints:**
    - short_id ensures each short link is distinct.
    - url_hash detects duplicates for the same normalized URL.
    - Postgres automatically creates indexes for these constraints.
- **Data Fields**: Each mapping stores shortId, originalUrl, normalizedUrl, urlHash, and createdAt.
- **Validation**: Comprehensive URL validation including length limits, format checks, and security considerations.
  - **Error Handling**: Standardized error responses with detailed messages and proper HTTP status codes.
- **Health Monitoring**: Spring Boot Actuator for basic health checks and monitoring.

## Future Improvements
- **Caching**:
    - Cache for frequently accessed URLs (e.g., Redis)

- **Link Management**:
    - Link expiration with TTL
    - Custom short IDs
    - URL deactivation option
    - Basic usage statistics

- **Observability**:
    - Metrics
    - Structured logging
    - Error tracking with Sentry
    - Request tracing

- **API Enhancements**:
    - Batch operations
    - Simple analytics endpoint

