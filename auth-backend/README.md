# Auth Backend

Clean JWT authentication API with Spring Boot. Handles login, registration, password reset, and all the auth stuff you
need. Built for developers who want something that just works.

## Tech Stack

- Spring Boot 3.5.0 + Spring Security 6
- JWT (access + refresh tokens) with RSA signing
- MySQL + JPA for persistence
- Email integration (Thymeleaf + Mailtrap)
- Comprehensive testing with JUnit 5

## Features

### Core Auth

- Register, Login, Logout with secure JWT
- Auto token refresh
- BCrypt password hashing (12 rounds)
- Rate limiting on auth endpoints

### Email Flows

- Welcome email on signup
- Password reset via secure token
- Clean HTML templates with Thymeleaf

### Security

- RSA-signed JWTs (RS256)
- Refresh token rotation
- Environment-based secrets
- Production-ready profiles

## Quick Start

```bash
git clone https://github.com/pw2712gz/auth-backend.git
cd auth-backend
```

**Prerequisites:** Java 17+, Maven, MySQL

**Generate RSA keys:**

```bash
# Inside src/main/resources/keys/
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in private.pem -out public.pem
```

**Setup database:**

```sql
CREATE DATABASE authdb;
CREATE USER 'authuser'@'localhost' IDENTIFIED BY 'securepassword';
GRANT ALL PRIVILEGES ON authdb.* TO 'authuser';
```

**Run it:**

```bash
./mvnw spring-boot:run
```

API available at `http://localhost:8080`

## API Endpoints

| Method | Endpoint                    | What it does         |
|--------|-----------------------------|----------------------|
| POST   | `/api/auth/register`        | Sign up new user     |
| POST   | `/api/auth/login`           | Login + get tokens   |
| POST   | `/api/auth/refresh`         | Refresh access token |
| POST   | `/api/auth/logout`          | Logout + cleanup     |
| GET    | `/api/auth/me`              | Get user profile     |
| POST   | `/api/auth/forgot-password` | Send reset email     |
| POST   | `/api/auth/reset-password`  | Reset with token     |

## Email Setup

Uses **Mailtrap** for dev testing:

1. Sign up at mailtrap.io
2. Copy SMTP settings to `application-dev.properties`
3. Done - emails work locally

## Testing

```bash
./mvnw test
```

Full test coverage for:

- All core services (`AuthService`, `JwtProvider`, `MailService`, etc.)
- Integration tests for `AuthController`
- Edge cases and error handling

## Structure

```
auth-backend/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data layer
├── entity/         # JPA models
├── dto/           # Request/response objects
├── config/        # Spring config
├── security/      # JWT + security setup
└── templates/     # Email templates
```

## Frontend

Pairs perfectly with the **Angular 19 Auth Frontend** - check it out [here](

## License

MIT License - feel free to use this as a starting point for your own projects.