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

> *
> ![screencapture-auth-ayubyusuf-dev-forgot-password-2025-06-11-00_13_16](https://github.com/user-attachments/assets/0c342124-487d-49e6-aa7c-22b9e4085343)
![screencapture-auth-ayubyusuf-dev-dashboard-2025-06-11-00_10_32](https://github.com/user-attachments/assets/12ceb242-f312-4ecc-aa61-85099725de72)
![screencapture-auth-ayubyusuf-dev-register-2025-06-11-00_12_50](https://github.com/user-attachments/assets/9f096293-74ca-43dc-81c9-1565c7a14de0)
![screencapture-auth-ayubyusuf-dev-login-2025-06-11-00_12_42](https://github.com/user-attachments/assets/c00f2ecc-c1f1-4095-a75c-1c9c2c89c0fa)
*

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
