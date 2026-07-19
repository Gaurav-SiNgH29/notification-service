# Notification Service

A backend service that sends notifications to users across multiple channels — Email, SMS, Push, and In-App. It checks each user's preferences before dispatching and logs every attempt to the database.

---

## What It Does

You send one API request with a user ID, a message, and a list of channels. The service checks which channels that user has opted into, dispatches only to those, and records every outcome.

For example, if you request `["EMAIL", "SMS", "PUSH"]` but the user has only opted into `EMAIL`, only the email gets sent. SMS and Push are skipped and logged as such.

---

## Tech Stack

- Java 17
- Spring Boot 4.1.0
- MySQL
- Spring Data JPA
- Spring Security (API Key auth)

---

## Structural Approach

The service is built using a layered architecture. Each layer has one job and only talks to the layer directly below it.

```
Request → Security → Controller → Service → Repository → Database
                                     ↓
                                  Channel Providers
```

**Security** checks the API key before anything else runs. If the key is missing or wrong, the request stops here.

**Controller** receives the HTTP request, validates the input, calls the service, and decides the HTTP status code based on the results. No business logic lives here.

**Service** is where all the decisions happen — find the user, fetch their preferences, compute which channels to actually dispatch to, call each provider, save the outcome. This is the core of the application.

**Channel layer** is built around a `NotificationChannel` interface. Each channel (Email, SMS, Push, In-App) implements this interface independently. The service does not know or care which provider it is talking to — it just calls `send()`. This means adding a fifth channel in the future requires adding one class and nothing else changes.

**Repository** layer talks to the database via Spring Data JPA. No raw SQL anywhere — just Java interfaces and method names that Spring converts to queries automatically.

**Exception handling** is centralised in one `GlobalExceptionHandler` class. The service throws meaningful exceptions and the handler converts them to clean JSON responses. No try/catch blocks scattered through the code.

```
src/main/java/com/indiagold/notification_service/
│
├── controller/        → Single REST endpoint
├── service/           → Business logic — preference checks, routing, history
├── channel/           → NotificationChannel interface + mock providers for each channel
├── domain/            → JPA entities and enums
├── repository/        → Database access via Spring Data JPA
├── dto/               → Request and response objects
├── security/          → API key filter and Spring Security config
└── exception/         → Custom exceptions and global error handler
```

---

## Database Design

Four tables. Each has a clear responsibility.

| Table | Purpose |
|---|---|
| `users` | Basic user profiles |
| `user_preferences` | Which channels each user has opted into |
| `notifications` | The original notification request — title, body, who it was for |
| `notification_history` | Per-channel dispatch outcome — what happened for each channel |

When one notification request comes in for 3 channels, it creates 1 row in `notifications` and 3 rows in `notification_history`. This keeps the original message separate from the dispatch outcomes.

---

## Prerequisites

- Java 17
- Maven
- MySQL running locally

---

## Setup

**1. Create the database**

Open MySQL and run:

```sql
CREATE DATABASE notification_db;
CREATE USER 'notif_user'@'localhost' IDENTIFIED BY 'notif_pass';
GRANT ALL PRIVILEGES ON notification_db.* TO 'notif_user'@'localhost';
```

**2. Clone the repo and run**

```bash
git clone https://github.com/GAUrav-sINGH-2-9/notification-service.git
cd notification-service
mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run          # Mac/Linux
```

The app starts on `http://localhost:8081`.

Tables are created automatically on first run. Three test users are seeded into the database on every startup so you can test immediately.

---

## Test Users (Pre-loaded)

| User ID | Name    | Opted Into              |
|---------|---------|-------------------------|
| 1       | Alice   | EMAIL, SMS, PUSH, IN_APP |
| 2       | Bob     | EMAIL, IN_APP only       |
| 3       | Charlie | Nothing (opted out)      |

---

## Authentication

Every request needs this header:

```
X-API-Key: indiagold-secret-key-2026
```

Missing or wrong key returns `401`.

---

## The Endpoint

```
POST http://localhost:8081/api/v1/notifications
```

**Request body:**

```json
{
  "userId": 1,
  "title": "Order Confirmed",
  "body": "Your order #1234 has been placed.",
  "channels": ["EMAIL", "SMS", "PUSH", "IN_APP"]
}
```

All four fields are required. Channel values are case-insensitive — `"email"` and `"EMAIL"` both work.

---

## Response

Each channel in your request gets a result:

```json
{
  "userId": 1,
  "results": [
    { "channel": "EMAIL",  "status": "DELIVERED" },
    { "channel": "SMS",    "status": "DELIVERED" },
    { "channel": "PUSH",   "status": "FAILED"    },
    { "channel": "IN_APP", "status": "DELIVERED" }
  ]
}
```

**Status meanings:**
- `DELIVERED` — dispatched successfully
- `FAILED` — dispatch attempted but failed
- `SKIPPED` — user has not opted into this channel

**HTTP status codes:**
- `200` — all channels delivered or skipped
- `207` — at least one failed but not all
- `500` — every channel failed
- `404` — user not found
- `400` — missing or invalid request fields
- `401` — invalid or missing API key

---

## Testing With Postman

**Headers (add to every request):**
```
X-API-Key: indiagold-secret-key-2026
Content-Type: application/json
```

**Test 1 — Alice, all channels:**
```json
{
  "userId": 1,
  "title": "Order Confirmed",
  "body": "Your order is ready.",
  "channels": ["EMAIL", "SMS", "PUSH", "IN_APP"]
}
```
Expected: All four channels attempted.

**Test 2 — Bob, all channels requested:**
```json
{
  "userId": 2,
  "title": "Payment Received",
  "body": "We received your payment.",
  "channels": ["EMAIL", "SMS", "PUSH", "IN_APP"]
}
```
Expected: EMAIL and IN_APP attempted. SMS and PUSH skipped.

**Test 3 — Charlie, opted out of everything:**
```json
{
  "userId": 3,
  "title": "Welcome",
  "body": "Welcome to the platform.",
  "channels": ["EMAIL", "SMS", "PUSH", "IN_APP"]
}
```
Expected: All four skipped. Returns `200`.

**Test 4 — User does not exist:**
```json
{
  "userId": 99,
  "title": "Test",
  "body": "Hello.",
  "channels": ["EMAIL"]
}
```
Expected: `404 Not Found`

**Test 5 — Missing field:**
```json
{
  "userId": 1,
  "body": "Missing title field.",
  "channels": ["EMAIL"]
}
```
Expected: `400 Bad Request`

---

## Testing With cURL

```bash
# Alice — all channels
curl -X POST http://localhost:8081/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: indigold-secret-key-2024" \
  -d '{
    "userId": 1,
    "title": "Order Confirmed",
    "body": "Your order is ready.",
    "channels": ["EMAIL", "SMS", "PUSH", "IN_APP"]
  }'

# Wrong API key
curl -X POST http://localhost:8081/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: wrong-key" \
  -d '{
    "userId": 1,
    "title": "Test",
    "body": "Test body.",
    "channels": ["EMAIL"]
  }'
```

---

## Running Unit Tests

```bash
mvnw.cmd test        # Windows
./mvnw test          # Mac/Linux
```

8 tests covering the core routing and preference logic — no database or network needed to run them.

---

## Checking the Database

Open MySQL Workbench and query directly:

```sql
USE notification_db;

SELECT * FROM users;
SELECT * FROM user_preferences;
SELECT * FROM notifications;
SELECT * FROM notification_history;
```

Every dispatch attempt is logged in `notification_history` — including skipped channels.
