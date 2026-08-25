# Scala Web API

A Play Framework-based REST API service written in Scala.

## Features

- RESTful API endpoints
- User authentication
- XML configuration parsing
- Session management
- User profile management

## Requirements

- Java 21.0.6
- Gradle 9.0.0
- Play Framework 3.0.0

## Build

```bash
./gradlew clean build
```

## Run

```bash
./gradlew run
```

## API Endpoints

### User Authentication
```
POST /api/auth/login
Content-Type: application/json
{
  "username": "user1",
  "password": "password123"
}
```

### Get User Profile
```
GET /api/users/{userId}
```

### Get User Bio
```
GET /api/users/{userId}/bio?bio={bioContent}
```

### Parse User XML
```
POST /api/users/parse-xml
Content-Type: application/xml
```

## Architecture

- **controllers**: API endpoints
- **services**: Business logic
- **models**: Data models

## License

MIT
