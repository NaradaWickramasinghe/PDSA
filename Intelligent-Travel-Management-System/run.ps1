# PowerShell script to run the Intelligent Travel Management System
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Starting Intelligent Travel Management System (Module 4 Engine) " -ForegroundColor Green
Write-Host " Web Dashboard: http://localhost:8085" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan

$mvnw = Join-Path $PSScriptRoot "mvnw.cmd"

Push-Location $PSScriptRoot
try {
    if (-not $env:DB_URL) {
        Write-Host "[INFO] No DB_URL specified. Running in standalone local mode (in-memory H2)..." -ForegroundColor Green
        & $mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
    }
    else {
        Write-Host "[INFO] Connecting to database: $env:DB_URL" -ForegroundColor Green
        & $mvnw spring-boot:run
    }
}
finally {
    Pop-Location
}
