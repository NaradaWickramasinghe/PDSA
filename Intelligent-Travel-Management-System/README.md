# Intelligent Travel Management System - Group 4 Backend

This folder contains the Spring Boot 3.3.2 project for **Group 4 (Intelligent Decision Engine)**.

## Project Structure
```
src/main/java/com/nibm/intelligenttravelmanagementsystem/
├── IntelligentTravelManagementSystemApplication.java
└── intelligentdecision/
    ├── config/       # Sample Data Seeding
    ├── controller/   # DecisionController (/api/decisions/recommend)
    ├── dto/          # RecommendationRequest & RecommendationResponse DTOs
    ├── model/        # JPA Entities (TravelerProfile, Destination, TravelHistory, DecisionLog)
    ├── repository/   # Spring Data JPA Repositories
    └── service/      # Decision Tree, KNN, Ranking Engine, Feature Preprocessing
```

## Running the Application

### Option 1: Standalone Local Mode (In-Memory H2)
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

### Option 2: PostgreSQL / Supabase Mode
```powershell
.\mvnw.cmd spring-boot:run
```

For complete algorithm details and Postman testing guide, see **[PROJECT.md](../PROJECT.md)**.
