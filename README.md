
## Requirements

- Docker Desktop
- Java 21+
- Node.js 20+

## Run Database

```bash
cd backend
docker compose up -d
```

Compose uses defaults that match the backend (`fyo_platform` / `fyo_user` / `fyo_password` on port `5432`).  
Optional: `cp .env.example .env` if you want to override ports or credentials.

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

### Firebase Admin credentials (needed for /api/auth)

The auth endpoints (`/api/auth/signup`, `/api/auth/login`) verify Firebase ID
tokens with the Firebase Admin SDK, which needs a service account key:

1. Firebase console → Project settings → Service accounts → Generate new
   private key. Save the JSON somewhere outside the repo (never commit it).
2. Point the backend at it before starting:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\path\to\serviceAccountKey.json"
.\mvnw.cmd spring-boot:run
```

```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/serviceAccountKey.json
./mvnw spring-boot:run
```

Without it the backend still starts and everything except `/api/auth/*` works;
auth calls return a clear error asking for the credentials.

## Run Frontend

```bash
cd frontend
cp .env.example .env   # needed for Firebase; Teams API defaults to http://localhost:8081
npm install            # first clone, or after dependency changes
npm run dev
```

Frontend runs at:

```text
http://localhost:5173
```

Open **Teams** in the top nav (or go to `http://localhost:5173/#/teams`) with the backend running to browse seeded teams.

## Notes

Flyway runs database migrations automatically when the backend starts.

To reset the local database during development:

```bash
cd backend
docker compose down -v
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

sanam dapushavt manamde ecadet bevri state daacomitot ro rame gaketebuli varianti tu mogwont sanam rames daamatebt (vtqvat axalis damatebit ro gafuwdes) dzveli ro ar dagekargot daacomitet xolme aq davwer komandebs 
g
```bash
git add .
git commit -m "komitis mokle agwera"
```



After finishing the task, push the branch and open a pull request:

```bash
git push -u origin tqveni_taskis_saxeli_mokled
```
pull requestebs vxsnit githubidan, ragacas ro dapushavt mere amogigdebt gitze create pull request da tqveni branchis saxels magas daawert da request sheiqmneba !!!merge to main ro aweria pirdapir magas ar daawirot (chem shecdomebs nu gaimeorebt dd )
Changes should be merged into `main` only through pull requests.
[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/skmUAHf8)
# Final-Project
OOP ფინალური პროექტი
