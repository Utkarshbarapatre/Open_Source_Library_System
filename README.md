# Open-Source Library System (OSLS)
**Buzzword Software Solutions Limited — Software Development Department**

---

## Tech Stack
- Java 17, Spring Boot 3.2.5, Maven
- Spring Security + JWT (jjwt 0.11.5)
- Spring Data JPA + Hibernate + MySQL
- Vanilla HTML/CSS/JS frontend (MVC pattern)

---

## Setup

### 1. MySQL Database
You can run MySQL in a Docker container using the following command:
```bash
docker run --name osls-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=utkarsh123 -e MYSQL_DATABASE=osls_db -d mysql:8.0
```
This command maps port `3306`, creates the database `osls_db`, and configures the password `utkarsh123` which matches the default values in `application.properties`.

If you prefer to use an existing MySQL instance, make sure it is running on port 3306 and matches the credentials in `application.properties`.

### 2. Run the App
```bash
mvn spring-boot:run
```
App starts at: **http://localhost:8080**

### 3. Create Admin User
After registering normally, open MySQL and run:
```sql
USE osls_db;
UPDATE users SET role='ADMIN' WHERE username='your_username';
```

---

## Project Structure
```
src/main/java/com/buzzword/osls/
├── config/         SecurityConfig.java
├── controller/     AuthController, ResourceController, CommentController, AdminController
├── dto/            Request/Response DTOs
├── model/          User, Resource, Comment entities + enums
├── repository/     JPA Repositories
├── security/       JwtUtil, JwtFilter, CustomUserDetailsService
└── service/        AuthService, ResourceService, CommentService, UserService

src/main/resources/static/
├── index.html      Login / Register
├── dashboard.html  Browse & search resources
├── resource.html   Add/Edit resource + comments
├── admin.html      Admin panel
├── css/style.css
└── js/             auth.js, dashboard.js, resource.js, admin.js
```

---

## API Endpoints

| Method | Path | Auth |
|---|---|---|
| POST | /api/auth/register | Public — always creates a USER account |
| POST | /api/auth/login | Public — any role |
| POST | /api/auth/admin-login | Public endpoint, but only succeeds for accounts with role=ADMIN |
| GET | /api/resources | USER |
| GET | /api/resources/search?q=&category= | USER |
| POST | /api/resources | USER |
| PUT | /api/resources/{id} | Owner or ADMIN |
| DELETE | /api/resources/{id} | Owner or ADMIN |
| GET | /api/resources/{id}/comments | USER |
| POST | /api/resources/{id}/comments | USER |
| GET | /api/comments/{id} | USER |
| PUT | /api/comments/{id} | Owner only (ADMIN cannot edit others' comments) |
| DELETE | /api/comments/{id} | Owner or ADMIN |
| GET | /api/admin/users | ADMIN only |
| GET | /api/admin/users/{id} | ADMIN only |
| DELETE | /api/admin/users/{id} | ADMIN only |
| GET | /api/admin/resources/{id} | ADMIN only |
| DELETE | /api/admin/resources/{id} | ADMIN only |
| GET | /api/admin/comments/{id} | ADMIN only |
| DELETE | /api/admin/comments/{id} | ADMIN only |
"# OSLS"

---

## Roles & Privileges

Two roles exist: `USER` and `ADMIN`.

- **USER** — can create resources/comments, and edit or delete *only the ones they created*. Cannot reach any `/api/admin/**` endpoint or the admin-login flow.
- **ADMIN** — can do everything a USER can, plus delete *any* user's resources or comments and manage user accounts. **Admins can delete other users' comments but cannot edit them** — comment editing is owner-only by design, so a comment's content always reflects what its actual author wrote. Admins is the only role that can authenticate via `/api/auth/admin-login`.

Ownership vs. admin access is enforced in `ResourceService` / `CommentService` (resource/comment writes), and role-only access is enforced at the security-filter level for `/api/admin/**` in `SecurityConfig`. The frontend (`resource.js`) also hides the Edit button on comments you don't own, even for admins — Delete stays visible for owner or admin.

**Login endpoints**
- `POST /api/auth/login` — works for both USER and ADMIN accounts, returns a JWT with the account's actual role.
- `POST /api/auth/admin-login` — same request body (`username`, `password`), but rejects the login with `403` if the account's role isn't `ADMIN`, even if the password is correct. Intended for an admin-only login page in the frontend.

Both endpoints issue the same kind of JWT — the token itself doesn't encode "how" the user logged in. Every request still re-checks the caller's role fresh from the database on each call (`CustomUserDetailsService`), so authorization can never go stale even if a user's role changes between logins.

---

## Admin Panel — Bug Fix Notes

`GET /api/admin/users` previously returned the raw `User` JPA entity, including the hashed password field and lazy-loaded `resources`/`comments` collections. Serializing those lazy collections outside an open Hibernate session threw an exception, so the endpoint returned `500` and the admin panel's user table failed to load. This is fixed by routing the response through a dedicated `UserResponse` DTO (`id`, `username`, `email`, `role`, `createdAt` only — no password, no lazy collections).

`admin.js`'s `loadUsers()`/`loadResources()` now also check `res.ok` before parsing the response, so a future backend error shows a clear "Failed to load… (status NNN)" message in the panel instead of failing silently.

---

## Dummy / Seed Data

`src/main/resources/data.sql` auto-loads on every app startup (`spring.sql.init.mode=always`, applied after Hibernate creates/updates tables via `spring.jpa.defer-datasource-initialization=true`). Inserts use `INSERT IGNORE`, so restarting the app on an existing database won't fail or duplicate rows.

**Users**

| Username | Password | Role |
|---|---|---|
| admin | Admin123! | ADMIN |
| jdoe | Password123! | USER |
| asmith | Password123! | USER |

**Resources ("products")**

| Title | Category | Added by |
|---|---|---|
| Clean Code: A Handbook of Agile Software Craftsmanship | BOOK | jdoe |
| The Twelve-Factor App | WHITEPAPER | asmith |
| OWASP Top Ten | STANDARD | admin |

Each resource also has 1–2 sample comments seeded against it. Log in with any of the accounts above via `POST /api/auth/login` to get a JWT and start testing the other endpoints right away — no manual `UPDATE users SET role='ADMIN'` step needed for the admin account.

---

## Docker Deployment

To build and run the entire application stack (the Spring Boot app and MySQL database) in Docker containers, follow these steps:

### Prerequisites
- Docker and Docker Compose installed on your system.

### Running with Docker Compose
1. **Start the stack** (builds the app image and starts both containers):
   ```bash
   docker-compose up --build -d
   ```

2. **Check container logs**:
   ```bash
   docker-compose logs -f
   # Or specifically for the application container:
   docker-compose logs -f app
   ```

3. **Access the application**:
   Open your browser to [http://localhost:8080](http://localhost:8080).

4. **Stop the containers**:
   ```bash
   docker-compose down
   ```
   To also remove the persistent database volume and start fresh:
   ```bash
   docker-compose down -v
   ```

---

## Render Deployment (Blueprint)

This repository includes a `render.yaml` file to deploy the entire stack on Render in one go:

1. Log in to [Render.com](https://render.com/).
2. Click **New +** -> **Blueprint**.
3. Select your GitHub repository.
4. Render will automatically configure the private MySQL container (with persistent disk) and the Spring Boot Web Service.
5. Click **Approve** to deploy both services.

