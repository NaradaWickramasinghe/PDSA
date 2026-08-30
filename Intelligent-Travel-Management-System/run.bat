@echo off
echo ======================================================================
echo  Starting Intelligent Travel Management System (Module 4 Engine)
echo  Web Dashboard: http://localhost:8085
echo ======================================================================

cd /d "%~dp0"
call mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
