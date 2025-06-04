# Stage 1 – Build
FROM node:20-alpine AS builder
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build

# Stage 2 – Serve with Nginx
FROM nginx:alpine
COPY --from=builder /app/dist/auth-frontend /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
