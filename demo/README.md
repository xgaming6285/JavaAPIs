# Admin Dashboard for Java APIs

This project is a comprehensive admin dashboard for Java APIs with a Spring Boot backend and React frontend. It provides functionality for viewing and managing user activities, analytics, sessions, audit logs, and importing users.

## Features

- User Activity Logging and Monitoring
- Analytics Events Tracking
- Session Management
- Audit Logging
- User Import via CSV

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Data MongoDB
- Spring Security Crypto
- Resilience4j for fault tolerance

### Frontend
- React 18
- TypeScript
- Material UI
- React Router
- Axios for HTTP requests

## Prerequisites

- Java 17 or higher
- Maven
- Node.js and npm (for frontend development)
- MongoDB (can use embedded for testing)

## Running the Application

### Manual Setup (Recommended)

You can use the provided setup scripts to simplify the frontend setup:

**Windows:**
```bash
setup-frontend.bat
```

**Unix/Linux/Mac:**
```bash
chmod +x setup-frontend.sh
./setup-frontend.sh
```

Alternatively, you can manually set up the project:

1. **Build and run the backend:**

```bash
cd demo
mvn clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

2. **Separately build and run the frontend for development:**

```bash
cd demo/src/main/frontend
npm install --legacy-peer-deps
npm start
```

3. **For production, build the frontend and copy it to the static directory:**

```bash
cd demo/src/main/frontend
npm install --legacy-peer-deps
npm run build
```

Then copy the contents of the `demo/src/main/frontend/build` directory to `demo/src/main/resources/static`.

### Development Mode

For development, you can run the backend and frontend separately:

#### Backend

```bash
mvn spring-boot:run
```

#### Frontend

```bash
cd src/main/frontend
npm install --legacy-peer-deps
npm start
```

The frontend development server will start on port 3000 and proxy API requests to the backend on port 8080.

## API Endpoints

### User Activities
- `GET /api/logs/activity/user/{userId}` - Get activities for a user
- `POST /api/logs/activity` - Log a new user activity

### Analytics
- `GET /api/logs/analytics/type/{eventType}` - Get analytics by type
- `POST /api/logs/analytics` - Log a new analytics event

### Sessions
- `POST /api/logs/sessions` - Create a new session
- `PUT /api/logs/sessions/{sessionToken}` - Update session activity
- `DELETE /api/logs/sessions/{sessionToken}` - Invalidate a session

### Audit Logs
- `GET /api/logs/audit/user/{userId}` - Get audit logs for a user
- `POST /api/logs/audit` - Create a new audit log

### User Import
- `POST /api/v1/import/users` - Import users from CSV file

## Building for Production

### Backend Only

```bash
mvn clean package -DskipTests
```

### Frontend for Production

```bash
cd src/main/frontend
npm install --legacy-peer-deps
npm run build
```

Copy the build output to the backend's static resources directory:

```bash
mkdir -p ../resources/static
cp -r build/* ../resources/static/
```

## Testing

```bash
mvn test
```

## License

This project is licensed under the MIT License. 