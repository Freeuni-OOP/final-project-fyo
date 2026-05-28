
## Requirements

- Docker Desktop
- Java 21+
- Node.js 20+

## Run Database

```bash
cd backend
cp .env.example .env
docker compose up -d
```

Adminer is available at:

```text
http://localhost:8080
```

Login:

```text
System: PostgreSQL
Server: db
Username: fyo_user
Password: fyo_password
Database: fyo_platform
```

## Run Backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend runs at:

```text
http://localhost:8081
```

## Run Frontend

```bash
cd frontend
cp .env.example .env
npm install  (this is not required on every run only when you add new npm packages or pulling project  the first time )
npm run dev
```

Frontend runs at:

```text
http://localhost:5173
```

## Notes

Flyway runs database migrations automatically when the backend starts.

To reset the local database during development:

```bash
cd backend
docker compose down -v     (this deletes all the data in db frtxilad amis gashvebisas)
docker compose up -d
```
