# 3. Non-Functional Requirements

[← Back to Table of Contents](./00_table_of_contents.md)

---

## 3.1 Overview

| Category | Requirement | Target | Rationale |
|----------|-------------|--------|-----------|
| **Performance** | API response time (p95) | < 200 ms (excl. AI calls) | Snappy dashboard UX |
| **Performance** | AI generation latency (p95) | < 8 s | Acceptable for async background task |
| **Performance** | Extension detection latency | < 500 ms after page load | Real-time feel |
| **Scalability** | Concurrent users | 10,000 DAU at launch | Startup-scale initial target |
| **Scalability** | Database reads | 50,000 RPM | Dashboard + search load |
| **Availability** | Uptime SLA | 99.5% | Acceptable for non-critical tooling |
| **Security** | Token storage | AES-256 encrypted at rest | GitHub tokens are high-value targets |
| **Security** | API authentication | JWT with RS256 | Industry standard for stateless auth |
| **Security** | Data in transit | TLS 1.3 | Mandatory for OAuth flows |
| **Reliability** | Sync retry | 3 retries with exponential backoff | GitHub API intermittent failures |
| **Reliability** | Data durability | Daily automated backups | MySQL + S3 backup |
| **Maintainability** | Code coverage | ≥ 80% on backend | CI gate |
| **Maintainability** | API versioning | URI-based (`/api/v1/`) | Non-breaking evolution |
| **Observability** | Logging | Structured JSON logs (ELK-ready) | Debuggability |
| **Observability** | Metrics | Prometheus + Grafana | SRE readiness |

## 3.2 Performance Requirements

### Response Time Targets

| Operation | p50 | p95 | p99 | Notes |
|-----------|-----|-----|-----|-------|
| REST API (CRUD) | 30 ms | 200 ms | 500 ms | Cached responses |
| Full-text search | 50 ms | 200 ms | 400 ms | MySQL FULLTEXT index |
| Analytics queries | 100 ms | 300 ms | 800 ms | Redis-cached aggregates |
| AI generation | 3 s | 8 s | 15 s | Async, non-blocking |
| GitHub commit | 500 ms | 2 s | 5 s | Dependent on GitHub API |
| Extension detection | 50 ms | 500 ms | 1 s | DOM mutation observer |

### Throughput Targets

| Metric | Target | Growth Plan |
|--------|--------|-------------|
| API requests | 50,000 RPM | Horizontal scaling via ECS |
| Concurrent WebSocket connections | 5,000 | Sticky sessions + Redis pub/sub |
| Background jobs (sync) | 500/min | Redis Streams consumer groups |
| Database write TPS | 200 | Connection pooling + write batching |

## 3.3 Scalability Requirements

- **Horizontal:** Application tier scales via ECS task count (2 → 20 instances)
- **Vertical:** Database scales via instance class upgrade (t3.medium → r6g.2xlarge)
- **Read Scaling:** MySQL read replicas for analytics and search workloads
- **Cache Scaling:** Redis cluster mode for distributed caching
- **Async Scaling:** Redis Streams consumer groups with independent scaling

## 3.4 Security Requirements

| Requirement | Standard | Implementation |
|-------------|----------|---------------|
| Authentication | OAuth 2.0 + PKCE | GitHub OAuth with state parameter |
| Authorization | RBAC | Resource-level ownership checks |
| Encryption at rest | AES-256-GCM | Application-level for tokens, TDE for database |
| Encryption in transit | TLS 1.3 | All external communication |
| Secret management | AWS Secrets Manager | No secrets in code or environment files |
| Dependency scanning | OWASP Top 10 | Automated in CI pipeline |
| Input validation | OWASP guidelines | Jakarta Bean Validation + parameterized queries |

## 3.5 Reliability Requirements

| Requirement | Target | Strategy |
|-------------|--------|----------|
| Data durability | 99.99% | Multi-AZ RDS with automated backups |
| Sync reliability | 99.5% success rate | Retry with exponential backoff, dead-letter queue |
| Graceful degradation | Core sync works without AI | AI failure doesn't block GitHub push |
| Disaster recovery | RPO: 1 hour, RTO: 4 hours | Automated RDS snapshots + S3 backup |

## 3.6 Availability Requirements

| Component | Target Uptime | Strategy |
|-----------|--------------|----------|
| Backend API | 99.5% | Multi-AZ ECS, health checks, auto-scaling |
| Database | 99.95% | RDS Multi-AZ with automatic failover |
| Redis Cache | 99.9% | ElastiCache with replica |
| Web Frontend | 99.9% | CloudFront + S3 (static hosting) |
| Extension | N/A (client-side) | Offline queue for resilience |

## 3.7 Maintainability Requirements

| Requirement | Target | Tooling |
|-------------|--------|---------|
| Code coverage (backend) | ≥ 80% | JaCoCo + CI gate |
| Code coverage (frontend) | ≥ 70% | Jest + React Testing Library |
| Static analysis | Zero critical issues | SonarQube / SpotBugs |
| API documentation | Always up-to-date | OpenAPI 3.0 auto-generated from code |
| Database migrations | Versioned, automated | Flyway |
| Dependency updates | Weekly automated PRs | Dependabot |

## 3.8 Observability Requirements

| Layer | Tool | Purpose |
|-------|------|---------|
| Logging | CloudWatch Logs (structured JSON) | Application logs, error tracking |
| Metrics | Prometheus + Grafana | Custom business metrics, JVM metrics |
| Tracing | AWS X-Ray | Distributed request tracing |
| Alerting | CloudWatch Alarms + SNS | SLA breach notifications |
| Uptime monitoring | AWS Synthetics | Canary endpoint checks every 5 min |

---

[← Previous: Functional Requirements](./02_functional_requirements.md) | [Next: System Architecture →](./04_system_architecture.md)
