# Auth Starter – Full Stack Auth Boilerplate

A clean, full-stack authentication starter built with Spring Boot 3 (Java) and Angular 19 (TypeScript) – ready for production and easy to deploy on AWS.

---

## What's Inside

### auth-backend/ – Spring Boot
- JWT authentication (access + refresh)
- Secure password hashing (BCrypt)
- Email reset and welcome flows using Resend
- Dockerized MySQL support
- Unit and integration tests for core features
- Hardened .env config and basic rate limiting

### auth-frontend/ – Angular 19
- Tailwind CSS styling
- Login, Register, Forgot/Reset Password views
- Auth guards, token refresh, and session handling
- Minimal UI with clean component structure
- Docker + S3/CloudFront ready

---

## Live Demo

Frontend: https://auth.ayubyusuf.dev  
Backend: Deployed via AWS App Runner

---

## Local Development

### Backend + MySQL

```bash
cd auth-backend
cp .env.example .env
docker compose -f docker-compose.backend.yml up --build
```

### Frontend

```bash
cd auth-frontend
npm install
npm run dev
```

---

## Screenshots

(Add your screenshots here)

---

## Deployment Summary

- Backend → AWS App Runner  
- Frontend → AWS S3 + CloudFront  
- Domain & DNS → Cloudflare  
- Email → Resend

For detailed setup, check each folder's README.

---

## Purpose

This project exists to serve as a real-world boilerplate for full-stack apps with clean authentication, deployable infrastructure, and zero bloat.

