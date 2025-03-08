# Spring Boot User Management API 🚀

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Custom-blue.svg)](LICENSE)

A robust and scalable User Management API built with Spring Boot, featuring comprehensive user operations, monitoring, and security features. This API provides a complete solution for user management with advanced features like email verification, password reset, and comprehensive monitoring.

## 🌟 Features

- **User Management**
  - CRUD operations for users
  - Email verification system
  - Password reset functionality
  - Search and filter capabilities
  - Role-based user management
  - Pagination and sorting support

- **Security & Performance**
  - BCrypt password encryption
  - Rate limiting for critical endpoints
  - Circuit breaker pattern implementation
  - Async operations support
  - Caching mechanism
  - Email verification workflow

- **Monitoring & Observability**
  - Prometheus metrics integration
  - Custom Grafana dashboards
  - Spring Boot Actuator endpoints
  - Comprehensive logging system
  - Health check endpoints
  - Performance metrics tracking

- **Documentation**
  - OpenAPI 3.0/Swagger integration
  - API versioning
  - Detailed endpoint documentation
  - Response examples
  - Error handling documentation

## 🚀 Quick Start

### Prerequisites

- JDK 17 or later
- Maven 3.6+
- Docker & Docker Compose (for containerization)
- SMTP Server access (for email functionality)

### Environment Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/xgaming6285/JavaAPIs.git
   cd demo
   ```

2. **Configure Email Settings**
   Create `application-local.properties` inside "demo\src\main\resources\" with your email configuration:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

### Running the Application

#### Local Development
1. **Build the application**
   ```bash
   ./mvn clean install
   ```

2. **Run the application**
   ```bash
   cd demo
   mvn spring-boot:run
   ```

   The application will start at `http://localhost:8080`

#### Docker Environment
1. **Start with Docker Compose**
   ```bash
   # For development with hot reload
   docker-compose up --build

   # For monitoring stack (includes Prometheus & Grafana)
   docker-compose -f docker-compose-monitoring.yml up --build
   ```

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

