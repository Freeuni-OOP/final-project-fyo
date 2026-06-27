
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
