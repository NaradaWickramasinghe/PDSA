@echo off
setlocal
echo ======================================================================
echo Starting Intelligent Travel Management System (Module 4 Engine)
echo Web Dashboard: http://localhost:8085
echo ======================================================================

if "%JAVA_HOME%"=="" (
    if exist "C:\Users\pcadmin\.antigravity\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64" (
        set "JAVA_HOME=C:\Users\pcadmin\.antigravity\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64"
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    )
)

echo Using Java: %JAVA_HOME%

pushd "%~dp0Intelligent-Travel-Management-System"

if "%DB_URL%"=="" (
    echo [INFO] No external DB_URL set. Starting in standalone local mode (in-memory H2)...
    call mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
) else (
    echo [INFO] Connecting to database: %DB_URL%
    call mvnw.cmd spring-boot:run
)

popd
pause
