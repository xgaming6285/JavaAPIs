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
   Create `application-local.properties` with your email configuration:
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
   ./mvnw clean install
   ```

2. **Run the application**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
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

### Swagger Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8080/api-docs`

### Key Endpoints
- User Registration: `POST /api/v1/users/register`
- User Login: `POST /api/auth/login`
- Password Reset: `POST /api/auth/reset-password`
- Email Verification: `GET /api/auth/verify`
- Health Check: `GET /api/health`

## 🔍 Monitoring Stack

### Available Endpoints
- Spring Actuator: `http://localhost:8080/actuator`
- Prometheus Metrics: `http://localhost:8080/actuator/prometheus`
- Grafana Dashboard: `http://localhost:3000`
  - Default credentials: admin/admin
  - Preconfigured dashboards available in `grafana/dashboards`

### Logging
- Application logs: `logs/application.log`
- Configurable log levels via Actuator

## 🛠️ Technology Stack

- **Core Framework**
  - Spring Boot 3.2.0
  - Spring Data JPA
  - Spring Security Crypto

- **Database**
  - H2 Database (dev/test)
  - Supports easy migration to PostgreSQL/MySQL

- **Monitoring & Reliability**
  - Prometheus
  - Grafana
  - Resilience4j
  - Spring Boot Actuator

- **Documentation & Testing**
  - SpringDoc OpenAPI
  - JUnit 5
  - Spring Boot Test

- **DevOps**
  - Docker
  - Docker Compose
  - Maven

## 📊 Project Structure
```
demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/demo/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── model/
│   │   │       └── exception/
│   │   └── resources/
│   └── test/
├── docker/
├── grafana/
│   ├── dashboards/
│   └── provisioning/
├── docker-compose.yml
├── docker-compose-monitoring.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## 🔒 Security Features

- BCrypt password encryption
- Rate limiting for authentication endpoints
- Email verification system
- Secure password reset workflow
- Circuit breaker for external services
- Input validation and sanitization

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under a custom license - see the [LICENSE](LICENSE) file for details.

## 📫 Support & Contact

For support and questions:
- Open an issue in the repository
- Contact: [Daniel](mailto:dani034406@gmail.com)

---

Made with ❤️ using Spring Boot

