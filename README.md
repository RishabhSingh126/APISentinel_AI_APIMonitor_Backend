# 🛡️ API Sentinel AI-Powered Monitoring - Core Engine & AI Backend

This repository contains the high-performance Spring Boot engine that powers the API Sentinel platform. It features automated background scheduling, multithreaded HTTP pinging, advanced mathematical metric calculations (p95 latency), and AI-driven root cause analysis.

## Core Technology Stack
* **Language:** Java 17+
* **Framework:** Spring Boot 3, Spring MVC, Spring Data JPA
* **Database:** MySQL 8.0 (Hibernate ORM)
* **Security:** Spring Security, Stateless JWT Authentication, BCrypt Hashing
* **AI Integration:** Spring AI (OpenAI/Gemini integrations)
* **Networking:** Spring WebFlux / RestClient
* **Deployment:** Docker containerized, hosted on Render

## The 13 Core System Features
1. **Automated Health Checks:** `@Scheduled` cron jobs sending non-blocking HTTP requests every minute.
2. **Uptime/Downtime Tracking:** Strict HTTP status code evaluation for success/failure logging.
3. **Deep Latency Measurement:** Tracks precise millisecond response times.
4. **Multi-Tenancy (JWT):** Secure, isolated API tracking for individual users and administrators.
5. **SSL/TLS Certificate Tracking:** Extracts `getNotAfter()` dates from certificates to trigger warnings for expiration within 30 days.
6. **Smart Retry Mechanism:** Suppresses false alarms by waiting 5 seconds and retrying twice before officially logging a "Down" status.
7. **Payload Keyword Validation:** Asserts JSON response bodies for user-defined required keywords (e.g., ensuring "id" exists in a 200 OK response).
8. **Advanced p95 Metrics:** Custom MySQL queries calculating the 95th percentile latency to expose how APIs perform under extreme stress.
9. **Discord Webhook Alerts:** Instantly pushes formatted error logs to user-provided Discord servers.
10. **Request Volume Tracking:** Aggregates time-series data to show exact traffic loads (hourly/daily/monthly).
11. **Cost per API Estimator:** Correlates request volume with user-defined base costs (e.g., AWS/OpenAI API pricing) to estimate monthly billing.
12. **AI-Powered Root Cause Analysis:** Feeds the last 5 failed logs into an LLM (via Spring AI) to generate human-readable explanations and suggested fixes.
13. **Chat With Your Logs:** An AI endpoint that allows users to ask plain-English questions about their historical API performance metrics.

## Architecture Overview
The system follows a strict Layered Architecture:
* `Controller`: Handles REST API routing and HTTP responses.
* `Service`: Contains core business logic (`PingService`, `AlertService`, `AiService`, `CostService`).
* `Repository`: Interfaces with MySQL via Spring Data JPA.
* `Security`: Manages JWT generation, validation filters, and CORS configurations.
* `DTO`: Data Transfer Objects ensuring database entities are never exposed directly to the client.

## Local Setup Instructions

### Prerequisites
* Java 17 JDK
* Maven
* MySQL Server 8.0 running locally on port `3306`

### Installation
1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/api-sentinel-backend.git](https://github.com/yourusername/api-sentinel-backend.git)
   cd api-sentinel-backend
