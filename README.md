# Spring Boot User Management API 🚀

<div align="center">

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Custom-blue.svg)](LICENSE)

</div>

<p align="center">
A robust and scalable User Management API built with Spring Boot, featuring comprehensive user operations, monitoring, and security features. This API provides a complete solution for user management with advanced features like email verification, password reset, role management, and comprehensive monitoring.
</p>

## 🛠️ Technology Stack

| Category | Technologies |
|----------|-------------|
| Core Framework | Spring Boot 3.2.0, Spring Data JPA, Spring Security Crypto |
| Database | H2 Database (dev/test), PostgreSQL/MySQL support |
| Monitoring | Prometheus, Grafana, Resilience4j, Spring Boot Actuator |
| Documentation & Testing | SpringDoc OpenAPI, JUnit 5, Spring Boot Test |
| DevOps | Docker, Docker Compose, Maven |

## 📑 Table of Contents
- [Features](#-features)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Configuration](#configuration)
- [API Documentation](#-api-documentation)
- [Monitoring & Analytics](#-monitoring--analytics)
- [Security & Performance](#-security--performance)
- [Contributing](#-contributing)
- [License](#-license)

## 🌟 Features

### Core Functionality
- ✅ Complete CRUD operations for user management
- 👥 Advanced role-based user management
  - Role assignment and removal
  - Multi-role support
  - Role-based filtering
- 📄 Pagination and sorting support
- 🔍 Advanced search and filter capabilities
- 📥 Bulk user import via CSV

### Security Features
- 🔒 BCrypt password encryption
- 📧 Email verification system
- 🔑 Password reset functionality
- ⚡ Rate limiting for critical endpoints
- 🔄 Circuit breaker pattern implementation

### Performance & Monitoring
- 📊 Prometheus metrics integration
- 📈 Custom Grafana dashboards
- 🔍 Spring Boot Actuator endpoints
- 📝 Comprehensive logging system
- ❤️ Health check endpoints
- 🚀 Async operations support
- 💾 Caching mechanism

## 🚀 Getting Started

### Prerequisites
| Requirement | Version |
|------------|---------|
| JDK | 17 or later |
| Maven | 3.6+ (optional if using mvnw) |
| Docker & Docker Compose | Latest |
| SMTP Server | - |

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/xgaming6285/JavaAPIs.git
   cd demo
   ```

2. **Build the Project**
   
   Using Maven Wrapper (Recommended):
   ```bash
   # Windows
   .\mvnw.cmd clean install

   # Linux/macOS
   chmod +x mvnw
   ./mvnw clean install
   ```

   Using Maven CLI:
   ```bash
   mvn clean install
   ```

3. **Run the Application**
   ```bash
   # Using Maven Wrapper
   ./mvnw spring-boot:run

   # Using Maven CLI
   mvn spring-boot:run
   ```

   The application will start at `http://localhost:8080`

### Configuration

1. **Email Setup**
   Create `application-local.properties` in "demo\src\main\resources\":
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

2. **Docker Environment**
   ```bash
   # Development with hot reload
   docker-compose up --build

   # Monitoring stack
   docker-compose -f docker-compose-monitoring.yml up --build
   ```

3. **Verify Installation**
   - API Documentation: `http://localhost:8080/swagger-ui.html`
   - Health Check: `http://localhost:8080/actuator/health`
   - API Base URL: `http://localhost:8080/api/v1`

## 📚 API Documentation

### Core Endpoints

#### User Management
| Endpoint | Method | Description | Rate Limited |
|----------|--------|-------------|--------------|
| `/api/v1/users` | GET | Get all users | No |
| `/api/v1/users` | POST | Create user | Yes |
| `/api/v1/users/{id}` | GET | Get user by ID | No |
| `/api/v1/users/{id}` | PUT | Update user | No |
| `/api/v1/users/{id}` | DELETE | Delete user | No |
| `/api/v1/users/search` | GET | Search users | No |
| `/api/v1/users/paginated` | GET | Get paginated users | No |

#### Authentication
| Endpoint | Method | Description | Rate Limited |
|----------|--------|-------------|--------------|
| `/api/auth/register` | POST | User registration | Yes |
| `/api/auth/login` | POST | User login | Yes |
| `/api/auth/verify` | GET | Email verification | No |
| `/api/auth/reset-password` | POST | Request password reset | Yes |

### Data Import
The user import feature accepts CSV files with the following format:
```csv
username,email,password,active,roles
john_doe,john.doe@example.com,password123,true,USER,ADMIN
jane_smith,jane.smith@example.com,password456,true,USER
```

## 🔍 Monitoring & Analytics

### Available Monitoring Tools
| Service | URL | Purpose |
|---------|-----|----------|
| Grafana | `http://localhost:3000` | Metrics visualization |
| Prometheus | `http://localhost:9090` | Metrics collection |
| Spring Actuator | `http://localhost:8080/actuator` | Application health |

### Analytics Endpoints
| Endpoint | Description |
|----------|-------------|
| `/api/v1/analytics/user-stats` | User statistics |
| `/api/v1/analytics/activity-trends` | User activity trends |
| `/api/v1/analytics/role-distribution` | Role distribution |
| `/api/v1/analytics/user-growth` | Growth metrics |

## 🔒 Security & Performance

### Rate Limiting Configuration
```properties
# Registration: 3 requests per minute
resilience4j.ratelimiter.instances.registration.limitForPeriod=3
resilience4j.ratelimiter.instances.registration.limitRefreshPeriod=1m

# Login: 5 requests per minute
resilience4j.ratelimiter.instances.login.limitForPeriod=5
resilience4j.ratelimiter.instances.login.limitRefreshPeriod=1m
```

## 🤝 Contributing
Please read our [Contributing Guidelines](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## 📄 License
This project is licensed under the terms of the [Custom License](LICENSE).

