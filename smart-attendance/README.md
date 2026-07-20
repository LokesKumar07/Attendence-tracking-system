# SmartAttend — Real-Time Attendance Management System

SmartAttend is a secure, responsive, production-ready attendance management application designed for teacher-only operation (supporting up to 50 students). It uses a clean white and violet theme.

## Tech Stack
* **Frontend**: React, TypeScript, Vite, Tailwind CSS, Axios, React Router, Recharts, Apache POI/jsPDF
* **Backend**: Spring Boot 3, Java 21, Maven, Spring Security (JWT authentication), STOMP WebSockets, Flyway migrations
* **Database**: MySQL 8 (InnoDB)

## Project Structure
```text
smart-attendance/
├── frontend/             # React SPA
├── backend/              # Spring Boot REST API
├── database/             # Schema & custom query assets
├── docker/               # Docker configurations
├── documentation/        # Technical documents
├── docker-compose.yml    # Local multi-container launch
└── README.md
```

## Quick Start
1. Setup your local Environment variables in `.env` matching `.env.example`.
2. Start the database and backend using Docker Compose:
   ```bash
   docker-compose up --build
   ```
3. Run the React frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
4. Authenticate using default teacher credentials:
   * **Username**: `lathika`
   * **Password**: `lathika123`

## Documentation Guides
Full guides are available in the [documentation/](./documentation) directory:
- [Deployment Guide](./documentation/DEPLOYMENT.md)
- [Security Auditing](./documentation/SECURITY.md)
- [Backup and Restore Protocols](./documentation/BACKUP_AND_RESTORE.md)
- [REST API Endpoints](./documentation/API_DOCUMENTATION.md)
- [Entity-Relationship Diagram](./documentation/DATABASE_SCHEMA.md)
- [Testing Protocols](./documentation/TESTING.md)
