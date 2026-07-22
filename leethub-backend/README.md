# LeetHub AI — Backend

Spring Boot 3.3 + Java 21 backend for LeetHub AI platform.

## Quick Start

### Prerequisites
- Java 21+
- Gradle 8.5+
- Docker (optional)

### Local Development

1. **Set up environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your local settings
   ```

2. **Start dependencies (Docker):**
   ```bash
   docker-compose -f docker/docker-compose.dev.yml up -d
   ```

3. **Build and run:**
   ```bash
   ./gradlew clean build
   ./gradlew bootRun
   ```

   Backend will be available at `http://localhost:8080/api/v1`

### Database Migrations

Migrations run automatically on startup (Flyway). To reset:
```bash
./gradlew flywayClean flywayMigrate
```

## Project Structure

```
src/main/java/com/leethubai/
├── common/          # Shared DTOs, exceptions, config
├── auth/           # Authentication & OAuth
├── sync/           # GitHub sync logic
├── ai/             # AI generation services
├── analytics/      # Analytics aggregation
├── search/         # Full-text search
├── notes/          # User notes
├── problems/       # Problem management
└── repository/     # GitHub repository management

src/main/resources/
├── db/migration/   # Flyway SQL migrations
└── application*.yml  # Configuration
```

## API Documentation

API docs available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

## Docker

Build and run with Docker:

```bash
# Development
docker-compose -f docker/docker-compose.dev.yml up

# Production (use .env file with secrets)
docker-compose -f docker-compose.prod.yml up
```

## Configuration

### Development (`application.yml`)
- SQLite in-memory database for testing
- Debug logging enabled
- CORS allows localhost:3000 and localhost:5173

### Production (`application-prod.yml`)
- MySQL database via RDS
- Structured JSON logging (ELK-ready)
- Health checks with liveness/readiness probes
- Metrics exported to Prometheus
- Rate limiting enabled
- Compression enabled

## Security

- JWT authentication with RS256
- GitHub OAuth 2.0 with PKCE
- AES-256-GCM encryption for sensitive data
- Rate limiting (60 req/min per user)
- CORS with strict headers
- Input validation on all endpoints
- SQL injection protection via JPA

## Performance

- Connection pooling (HikariCP): 10-30 connections
- Redis caching with 5-15 min TTL
- Pagination with keyset (cursor-based)
- Database indexes on frequent queries
- Async processing with Redis Streams

## Troubleshooting

### Port 8080 already in use
```bash
lsof -i :8080  # Find process
kill -9 <PID>   # Kill it
```

### Database connection errors
```bash
# Check MySQL is running
docker ps | grep mysql

# Restart MySQL
docker-compose -f docker/docker-compose.dev.yml restart mysql
```

### Cache issues
```bash
# Clear Redis
docker exec leethub-redis redis-cli FLUSHALL
```

## Contributing

1. Create feature branch: `git checkout -b feature/foo`
2. Commit changes: `git commit -am 'Add foo'`
3. Push to branch: `git push origin feature/foo`
4. Open pull request

All PRs must pass CI checks (tests, linting, security scans).

## License

MIT
