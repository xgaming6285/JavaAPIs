# Spring Boot User Management API 🚀

<div align="center">

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Custom-blue.svg)](LICENSE)

</div>

<p align="center">
A robust and scalable User Management API built with Spring Boot, featuring comprehensive user operations, monitoring, and security features. This API provides a complete solution for user management with advanced features like email verification, password reset, and comprehensive monitoring.
</p>

<details>
<summary>🌟 Features</summary>

### User Management
- ✅ CRUD operations for users
- 📧 Email verification system
- 🔑 Password reset functionality
- 🔍 Search and filter capabilities
- 👥 Role-based user management
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
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/users/register` | POST | User Registration |
| `/api/auth/login` | POST | User Login |
| `/api/auth/reset-password` | POST | Password Reset |
| `/api/auth/verify` | GET | Email Verification |
| `/api/health` | GET | Health Check |

### Documentation Links
- 📘 Swagger UI: `http://localhost:8080/swagger-ui.html`
- 📗 OpenAPI Spec: `http://localhost:8080/api-docs`

## 🔍 Monitoring Stack

<details>
<summary>Available Monitoring Endpoints</summary>

| Service | URL | Credentials |
|---------|-----|-------------|
| Spring Actuator | `http://localhost:8080/actuator` | - |
| Prometheus | `http://localhost:8080/actuator/prometheus` | - |
| Grafana | `http://localhost:3000` | admin/admin |

</details>

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

