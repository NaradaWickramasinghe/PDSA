# Intelligent Travel Management System (PDSA)

This project contains the **Intelligent Travel Management System** structured according to the modular monolith architecture for PDSA.

## Repository Structure

```
├── Intelligent-Travel-Management-System/   # Spring Boot Project Root
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/nibm/intelligenttravelmanagementsystem/
│   │   │   │   ├── IntelligentTravelManagementSystemApplication.java
│   │   │   │   ├── shared/                # Shared Domain Models (Node, Edge)
│   │   │   │   ├── intelligentdecision/   # Module 4: ML Decision Tree, k-NN, Ranking
│   │   │   │   ├── routesequencing/       # Module 4: Route Optimization & Sequencing (TSP)
│   │   │   │   └── common/                # Shared DTOs and Exception Handlers
│   │   │   └── resources/
│   │   │       ├── application.properties # PostgreSQL (Supabase) configuration
│   │   │       ├── application-local.properties # Standalone local H2 configuration
│   │   │       ├── db/migration/          # Flyway SQL migrations
│   │   │       └── static/                # Web Dashboard UI
│   │   └── test/                          # Unit & Integration Tests
│   └── pom.xml
├── run.ps1                                # PowerShell runner script
├── run.bat                                # Windows batch runner script
└── evaluate_models.py                     # Module 4 Evaluation Benchmarking
```

## Running the Application

### Option 1: Standalone Local Mode (In-Memory H2, No setup required)
```powershell
.\run.ps1
```
or run with batch script:
```cmd
run.bat
```

### Option 2: Connecting to Remote Database
```powershell
$env:DB_URL = "jdbc:postgresql://<host>:5432/<db>"
.\run.ps1
```

Once running, the interactive web dashboard is accessible at:
👉 **http://localhost:8085**
