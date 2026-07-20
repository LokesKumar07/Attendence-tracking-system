# Deployment Guide

This guide explains how to deploy SmartAttend frontend, backend, and database to production environments.

## Database (Cloud MySQL)
1. Provision a managed MySQL instance on **Railway**, **Aiven**, or **Clever Cloud**.
2. Run database migration using Spring Boot Flyway (runs automatically during container initialization).

## Backend (Render or Railway)
1. Connect your Github repository to Render or Railway.
2. Select **Docker** or **Web Service (Java)** as environment.
3. Configure Environment Variables:
   * `DATABASE_URL`: `jdbc:mysql://your-db-host:3306/smart_attendance`
   * `DATABASE_USERNAME`: Database username
   * `DATABASE_PASSWORD`: Database password
   * `JWT_SECRET`: Secure 64-character signing secret
   * `JWT_ACCESS_EXPIRATION`: Token timeout (e.g. `900000` = 15 mins)
   * `JWT_REFRESH_EXPIRATION`: Refresh token timeout (e.g. `604800000` = 7 days)
   * `FRONTEND_URL`: URL of the deployed React frontend (for CORS mapping)
   * `APP_TIMEZONE`: `Asia/Kolkata`

## Frontend (Vercel)
1. Import `frontend` project to Vercel dashboard.
2. Add Environment Variables:
   * `VITE_API_BASE_URL`: `https://your-backend-api.com/api/v1`
   * `VITE_WS_URL`: `wss://your-backend-api.com/ws`
3. Deploy! Vercel compiles production asset bundle automatically.
