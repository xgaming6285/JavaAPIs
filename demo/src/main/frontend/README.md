# Admin Dashboard Frontend

This is the frontend for the Admin Dashboard application built with React, TypeScript, and Material UI.

## Features

- User-friendly interface for managing and viewing data
- Dashboard with summary statistics
- User Activity logging and viewing
- Analytics data visualization
- Session management
- Audit logs
- User import via CSV

## Technology Stack

- React 18
- TypeScript 4
- Material UI for components
- React Router for navigation
- Axios for API communication

## Getting Started

### Development

1. Install dependencies:
```bash
npm install --legacy-peer-deps
```

2. Start the development server:
```bash
npm start
```

This will start the development server on port 3001 with proxy to the backend on port 8080.

### Production Build

1. Create a production build:
```bash
npm run build
```

2. Copy the build output to the Spring Boot static resources directory:
```bash
# From the frontend directory
mkdir -p ../resources/static
cp -r build/* ../resources/static/
```

## Project Structure

- `/components`: Reusable UI components
- `/pages`: Page components for different routes
- `/services`: API service functions
- `/types`: TypeScript interfaces for data models

## Available Scripts

- `npm start`: Runs the app in development mode
- `npm run build`: Builds the app for production
- `npm test`: Runs tests
- `npm run eject`: Ejects from Create React App configuration 