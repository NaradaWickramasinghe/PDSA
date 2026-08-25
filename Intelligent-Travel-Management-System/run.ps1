# PowerShell script to run the Intelligent Travel Management System
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Starting Intelligent Travel Management System (Module 4 Engine) " -ForegroundColor Green
Write-Host " Web Dashboard: http://localhost:8085" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan

if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $fallbackJdk = "C:\Users\pcadmin\.antigravity\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64"
    if (Test-Path $fallbackJdk) {
        $env:JAVA_HOME = $fallbackJdk
        $env:Path = "$fallbackJdk\bin;" + $env:Path
        Write-Host "Configured JAVA_HOME from environment: $fallbackJdk" -ForegroundColor Gray
    }
}

$projDir = Join-Path $PSScriptRoot "Intelligent-Travel-Management-System"
$mvnw = Join-Path $projDir "mvnw.cmd"

Push-Location $projDir
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
