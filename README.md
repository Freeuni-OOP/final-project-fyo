
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

## Team Workflow

Do not work directly on the `main` branch.

For every task, create a new branch:

```bash
git checkout main
git pull
git checkout -b tqveni_taskis_saxeli_mokled
```

After finishing the task, push the branch and open a pull request:

```bash
git push -u origin tqveni_taskis_saxeli_mokled
```

Changes should be merged into `main` only through pull requests.
