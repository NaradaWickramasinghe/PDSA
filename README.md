# Intelligent Travel Management System (PDSA) - Group 4 Backend

Welcome to the **Group 4 Subsystem** of the Intelligent Travel Management System (PDSA).

## Quick Links
- 📘 **[PROJECT.md](PROJECT.md)** - Full project documentation, machine learning algorithm explanations, architecture details, Supabase database guide, and Postman API manual.
- 🚀 **[Postman Collection](Group4_PDSA_Backend.postman_collection.json)** - Ready-to-import Postman Collection for testing REST endpoints.

## Group 4 Overview: Intelligent Decision Engine
The Intelligent Decision Engine provides personalized travel destination recommendations using:
1. **Decision Tree Classifier** for destination suitability prediction (`EXCELLENT`, `SUITABLE`, `MODERATE`, `UNSUITABLE`).
2. **K-Nearest Neighbors (KNN)** with Cosine Similarity over 6D preference vectors.
3. **Multi-Criteria Weighted Ranking Engine** for constraint-aware Top-N destination scoring.

## Quick Start Guide

### 1. Run Unit & Integration Tests
```powershell
cd Intelligent-Travel-Management-System
.\mvnw.cmd test
```

### 2. Start Backend Application
```powershell
cd Intelligent-Travel-Management-System
.\mvnw.cmd spring-boot:run
```
Backend will start on port `8085`.

### 3. Verify Health Check
- **GET** `http://localhost:8085/api/decisions/health`

### 4. Postman API Request
- **POST** `http://localhost:8085/api/decisions/recommend`
- **Header**: `Content-Type: application/json`
- **Payload**:
```json
{
  "budget": 1200.00,
  "durationDays": 5,
  "groupSize": 2,
  "ageGroup": "YOUNG_ADULT",
  "travelStyle": "COUPLE",
  "beachPreference": 9,
  "adventurePreference": 7,
  "naturePreference": 8,
  "culturePreference": 5,
  "nightlifePreference": 8,
  "relaxationPreference": 9,
  "topN": 5
}
```

For complete technical documentation, refer to **[PROJECT.md](PROJECT.md)**.
