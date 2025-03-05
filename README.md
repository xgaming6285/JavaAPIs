# Spring Boot User Management API 🚀

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A robust and scalable User Management API built with Spring Boot, featuring comprehensive user operations, monitoring, and security features.

## 🌟 Features

- **User Management**
  - CRUD operations for users
  - Password encryption
  - Search functionality
  - Pagination support

- **Security & Performance**
  - Circuit breaker pattern implementation
  - Async operations support
  - Caching mechanism
  - Password encryption

- **Monitoring & Observability**
  - Prometheus metrics integration
  - Grafana dashboards
  - Actuator endpoints
  - Comprehensive logging

- **Documentation**
  - OpenAPI/Swagger integration
  - API versioning
  - Detailed endpoint documentation

## 🚀 Quick Start

### Prerequisites

- JDK 17 or later
- Maven 3.6+
- Docker (optional, for containerization)

### Running the Application

1. **Clone the repository**
   ```bash
   git clone https://github.com/xgaming6285/JavaAPIs.git
   cd demo
   ```

2. **Build the application**
   ```bash
   ./mvnw clean install
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

   The application will start at `http://localhost:8080`

### Running with Docker

Build and run with Docker Compose
```bash
docker-compose up --build
```

## 📚 API Documentation

Once the application is running, you can access:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

## 🔍 Monitoring

### Metrics and Monitoring

- Actuator Endpoints: `http://localhost:8080/actuator`
- Prometheus Metrics: `http://localhost:8080/actuator/prometheus`
- Grafana Dashboard: `http://localhost:3000` (when running with docker-compose)

### Health Check
```bash
curl http://localhost:8080/api/health
```
## 🛠️ Technology Stack

- **Spring Boot** - Application framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Resilience4j** - Circuit breaker implementation
- **SpringDoc OpenAPI** - API documentation
- **Prometheus & Grafana** - Monitoring and metrics
- **Docker** - Containerization

## 📊 Project Structure

```
demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/demo/
│   │   └── resources/
│   └── test/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## 🔒 Security

- Password encryption using Spring Security Crypto
- Circuit breaker pattern for fault tolerance
- Rate limiting capabilities

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is licensed under the Apache License 2.0 - see [LICENSE](http://www.apache.org/licenses/LICENSE-2.0) for details.

## 📫 Support

For support and questions, please open an issue in the repository.

---

Made with ❤️ using Spring Boot

