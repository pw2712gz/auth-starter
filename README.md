# 🔐 Full Stack JWT Auth Starter

A clean, full-stack authentication starter built with **Spring Boot 3** (Java) and **Angular 19** (TypeScript). It’s production-ready and easy to deploy on AWS.

---

## 🚀 What's Inside

### `auth-backend/` – Spring Boot
- JWT authentication (access + refresh tokens)
- Secure password hashing with BCrypt
- Password reset & welcome email flows via **Resend**
- Dockerized MySQL support
- Unit & integration tests for core features
- Hardened `.env` config and basic rate limiting

### `auth-frontend/` – Angular 19
- Tailwind CSS styling
- Login, Register, Forgot/Reset Password views
- Auth guards, token refresh, and session handling
- Minimal UI with clean standalone component structure
- Docker + S3/CloudFront deployment ready

---

## 🌐 Live Demo

👉 [auth.ayubyusuf.dev](https://auth.ayubyusuf.dev)

---

## 💻 Local Development

### Backend + MySQL

\`\`\`bash
cd auth-backend
cp .env.example .env
docker compose -f docker-compose.backend.yml up --build
\`\`\`

### Frontend

\`\`\`bash
cd auth-frontend
npm install
npm run dev
\`\`\`

---

## 🖼️ Screenshots

> *(Add screenshots of login, dashboard, reset password, etc. here)*

---

## 🛠️ Deployment Summary

- **Backend** → AWS App Runner  
- **Frontend** → AWS S3 + CloudFront  
- **Domain & DNS** → Cloudflare  
- **Email** → Resend

➡️ For full setup instructions, see each folder’s `README`.

---

## 🎯 Purpose

This project is a real-world boilerplate for modern full-stack apps with secure authentication, clean architecture, and deployable infrastructure — with zero bloat.
