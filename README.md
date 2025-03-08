# Spring Boot User Management API 🚀

<div align="center">

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Custom-blue.svg)](LICENSE)

</div>

<p align="center">
A robust and scalable User Management API built with Spring Boot, featuring comprehensive user operations, monitoring, and security features. This API provides a complete solution for user management with advanced features like email verification, password reset, role management, and comprehensive monitoring.
</p>

<details>
<summary>🌟 Features</summary>

### User Management
- ✅ CRUD operations for users
- 📧 Email verification system
- 🔑 Password reset functionality
- 🔍 Search and filter capabilities
- 👥 Advanced role-based user management
  - Role assignment and removal
  - Multi-role support
  - Role-based filtering
- 📄 Pagination and sorting support

### Security & Performance
- 🔒 BCrypt password encryption
- ⚡ Rate limiting for critical endpoints
- 🔄 Circuit breaker pattern implementation
- 🚀 Async operations support
- 💾 Caching mechanism
- ✉️ Email verification workflow

### Monitoring & Observability
- 📊 Prometheus metrics integration
- 📈 Custom Grafana dashboards
- 🔍 Spring Boot Actuator endpoints
- 📝 Comprehensive logging system
- ❤️ Health check endpoints
- 📉 Performance metrics tracking

### Documentation
- 📚 OpenAPI 3.0/Swagger integration
- 🔢 API versioning
- 📖 Detailed endpoint documentation
- 💡 Response examples
- ⚠️ Error handling documentation

</details>

## 🚀 Quick Start

### Prerequisites
| Requirement | Version |
|------------|---------|
| JDK | 17 or later |
| Maven | 3.6+ |
| Docker & Docker Compose | Latest |
| SMTP Server | - |

### Environment Setup

<details>
<summary>1. Clone and Configure</summary>

   ```bash
   git clone https://github.com/xgaming6285/JavaAPIs.git
   cd demo
   ```

   Create `application-local.properties` inside "demo\src\main\resources\" with your email configuration:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```
</details>

<details>
<summary>2. Run the Application</summary>

#### Local Development

```bash
./mvn clean install
cd demo
mvn spring-boot:run
```
Application starts at `http://localhost:8080`

#### Docker Environment

Development with hot reload
```bash
docker-compose up --build
```

Monitoring stack (Prometheus & Grafana)
```bash
docker-compose -f docker-compose-monitoring.yml up --build
```
</details>

## 📚 API Documentation

### Available Endpoints

#### User Management
| Endpoint | Method | Description | Rate Limited |
|----------|--------|-------------|--------------|
| `/api/v1/users` | GET | Get all users | No |
| `/api/v1/users` | POST | Create user | Yes |
| `/api/v1/users/{id}` | GET | Get user by ID | No |
| `/api/v1/users/{id}` | PUT | Update user | No |
| `/api/v1/users/{id}` | DELETE | Delete user | No |
| `/api/v1/users/search` | GET | Search users by username | No |
| `/api/v1/users/paginated` | GET | Get paginated users | No |
| `/api/v1/users/{id}/password` | PUT | Update user password | Yes |
| `/api/v1/users/circuit-test/{id}` | GET | Test circuit breaker | No |
| `/api/v1/users/active` | GET | Get active users | No |
| `/api/v1/users/inactive` | GET | Get inactive users | No |
| `/api/v1/users/by-domain` | GET | Get users by email domain | No |
| `/api/v1/users/by-role` | GET | Get users by role | No |
| `/api/v1/users/by-min-roles` | GET | Get users by minimum roles | No |
| `/api/v1/users/search/advanced` | GET | Advanced user search | No |
| `/api/v1/users/{id}/roles` | PUT | Update user roles | No |

#### Authentication
| Endpoint | Method | Description | Rate Limited |
|----------|--------|-------------|--------------|
| `/api/auth/register` | POST | User registration | Yes |
| `/api/auth/login` | POST | User login | Yes |
| `/api/auth/verify` | GET | Email verification | No |
| `/api/auth/reset-password` | POST | Request password reset | Yes |
| `/api/auth/update-password` | POST | Update password with token | No |

#### Analytics
| Endpoint | Method | Description | Rate Limited |
|----------|--------|-------------|--------------|
| `/api/v1/analytics/user-stats` | GET | Get user statistics | No |
| `/api/v1/analytics/activity-trends` | GET | Get user activity trends | No |
| `/api/v1/analytics/role-distribution` | GET | Get role distribution analysis | No |
| `/api/v1/analytics/user-growth` | GET | Get user growth metrics | No |
| `/api/v1/analytics/security-metrics` | GET | Get security metrics | No |
| `/api/v1/analytics/user-retention` | GET | Get user retention metrics | No |
| `/api/v1/analytics/user-behavior` | GET | Get user behavior analysis | No |

#### Health Check
| Endpoint | Method | Description | Rate Limited |
|----------|--------|-------------|--------------|
| `/api/health` | GET | API health check | No |

### Rate Limiting Configuration
```properties
# Registration: 3 requests per minute
resilience4j.ratelimiter.instances.registration.limitForPeriod=3
resilience4j.ratelimiter.instances.registration.limitRefreshPeriod=1m

# Login: 5 requests per minute
resilience4j.ratelimiter.instances.login.limitForPeriod=5
resilience4j.ratelimiter.instances.login.limitRefreshPeriod=1m

# Password Reset: 3 requests per minute
resilience4j.ratelimiter.instances.passwordReset.limitForPeriod=3
resilience4j.ratelimiter.instances.passwordReset.limitRefreshPeriod=1m
```

### Documentation Links
- 📘 Swagger UI: `http://localhost:8080/swagger-ui.html`
- 📗 OpenAPI Spec: `http://localhost:8080/api-docs`

## 🔍 Monitoring Stack

### Available Monitoring Endpoints
| Service | URL | Description |
|---------|-----|-------------|
| Spring Actuator | `http://localhost:8080/actuator` | Health and metrics information |
| Prometheus | `http://localhost:8080/actuator/prometheus` | Prometheus metrics |
| Grafana | `http://localhost:3000` | Metrics visualization (credentials: admin/admin) |

### Metrics Available
- User creation count
- API response times
- Active/Inactive user counts
- Login attempts
- Rate limiter statistics
- Circuit breaker status
- User activity trends
- Security metrics
- User retention metrics
- Role distribution

## 🛠️ Technology Stack

<details>
<summary>View Full Stack</summary>

| Category | Technologies |
|----------|-------------|
| Core Framework | Spring Boot 3.2.0, Spring Data JPA, Spring Security Crypto |
| Database | H2 Database (dev/test), PostgreSQL/MySQL support |
| Monitoring | Prometheus, Grafana, Resilience4j, Spring Boot Actuator |
| Documentation & Testing | SpringDoc OpenAPI, JUnit 5, Spring Boot Test |
| DevOps | Docker, Docker Compose, Maven |

</details>

## 📫 Support & Contact

For support and questions:
- 🐛 [Open an issue](https://github.com/xgaming6285/JavaAPIs/issues)
- 📧 Contact: [Daniel](mailto:dani034406@gmail.com)

---

<div align="center">
Made with ❤️ using Spring Boot
</div>

