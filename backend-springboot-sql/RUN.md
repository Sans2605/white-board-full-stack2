# Whiteboard — Spring Boot + SQL Backend

The Node.js backend has been removed. This Spring Boot backend replaces it,
with **SQL (H2 / MySQL)** instead of MongoDB.

---

## Quick Start (H2 — zero setup)

```bash
cd backend-springboot-sql
./mvnw spring-boot:run
```

- REST API → http://localhost:8080
- Socket.IO server → ws://localhost:9090
- **H2 web console** → http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:whiteboarddb`
  - User: `sa` | Password: *(leave blank)*

> **Note:** H2 is an in-memory database. Data resets every time you restart.
> Switch to MySQL for permanent storage (see below).

---

## Switch to MySQL (permanent data)

1. Install MySQL and create a database:
   ```sql
   CREATE DATABASE whiteboarddb;
   ```

2. In `src/main/resources/application.properties`, comment out the H2 block
   and uncomment the MySQL block. Fill in your username/password.

3. In `pom.xml`, comment out the H2 dependency and uncomment the MySQL one.

4. Restart: `./mvnw spring-boot:run`

---

## SQL Tables Created Automatically

Hibernate creates these tables on first run:

| Table           | Columns                                                  |
|-----------------|----------------------------------------------------------|
| `users`         | id (PK), email (unique), password                        |
| `canvases`      | id (PK), owner_id (FK→users), elements_json (TEXT), created_at |
| `canvas_shared` | id (PK), canvas_id (FK→canvases), user_id (FK→users)    |

---

## API Endpoints (unchanged from Node.js version)

| Method | Path                      | Auth | Description             |
|--------|---------------------------|------|-------------------------|
| POST   | /api/users/register       | No   | Register                |
| POST   | /api/users/login          | No   | Login → returns JWT     |
| GET    | /api/users/me             | Yes  | Get current user        |
| POST   | /api/canvas/create        | Yes  | Create canvas           |
| GET    | /api/canvas/load/:id      | Yes  | Load canvas             |
| PUT    | /api/canvas/update        | Yes  | Update canvas elements  |
| PUT    | /api/canvas/share/:id     | Yes  | Share with another user |
| PUT    | /api/canvas/unshare/:id   | Yes  | Unshare                 |
| DELETE | /api/canvas/delete/:id    | Yes  | Delete canvas           |
| GET    | /api/canvas/list          | Yes  | List my canvases        |

Frontend code in `whiteboard-tutorial/` does **not** need any changes.

---

## Prerequisites

- Java 17+
- Maven (or use included `./mvnw` wrapper)
