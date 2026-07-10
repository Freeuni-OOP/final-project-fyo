$ErrorActionPreference = "Stop"

$root = $PSScriptRoot

Write-Host "Starting database..."
docker compose -f "$root\backend\docker-compose.yml" up -d db adminer

Write-Host "Waiting for database..."
$dbReady = $false
for ($i = 0; $i -lt 30; $i++) {
    docker exec fyo_platform_db pg_isready -U fyo_user -d fyo_platform *> $null
    if ($LASTEXITCODE -eq 0) {
        $dbReady = $true
        break
    }
    Start-Sleep -Seconds 1
}

if (-not $dbReady) {
    throw "Database did not become ready."
}

Write-Host "Starting backend..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\backend'; .\mvnw.cmd spring-boot:run"

Write-Host "Starting frontend..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\frontend'; npm run dev"

Write-Host ""
Write-Host "Running:"
Write-Host "Frontend: http://localhost:5173"
Write-Host "Backend:  http://localhost:8081"
Write-Host "Adminer:  http://localhost:8080"
