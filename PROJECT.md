# Group 4: Intelligent Decision Engine & Travel Recommendation System
## Project Architecture, File Structure, Machine Learning Algorithms & API Guide

---

## 1. Executive Summary

This project is the **Group 4 Backend Subsystem** for the **Intelligent Travel Management System (PDSA)**. 

The **Intelligent Decision Engine** combines statistical machine learning algorithms (Decision Tree Classifier, K-Nearest Neighbors) with custom multi-criteria scoring to provide personalized, intelligent travel destination recommendations for travelers based on their budget, duration, group size, travel style, and preference ratings across six key categories (Beach, Adventure, Nature, Culture, Nightlife, Relaxation).

---

## 2. Directory & File Structure Map

```
PDSA cw/
├── Group4_PDSA_Backend.postman_collection.json   # Ready-to-import Postman Collection
├── PROJECT.md                                    # Comprehensive project documentation
├── README.md                                     # Main repository README
└── Intelligent-Travel-Management-System/        # Spring Boot 3.3.2 Backend Project
    ├── pom.xml                                   # Maven dependencies & build config
    ├── mvnw / mvnw.cmd                           # Cross-platform Maven wrapper scripts
    ├── src/
    │   ├── main/
    │   │   ├── java/com/nibm/intelligenttravelmanagementsystem/
    │   │   │   ├── IntelligentTravelManagementSystemApplication.java # Spring Boot Main
    │   │   │   ├── common/                       # Shared Utilities
    │   │   │   │   ├── dto/
    │   │   │   │   │   └── ApiResponse.java       # Standard API Envelope Response
    │   │   │   │   └── exception/                # Global Exception Handling
    │   │   │   │       ├── GlobalExceptionHandler.java
    │   │   │   │       ├── InvalidRequestException.java
    │   │   │   │       ├── MlInferenceException.java
    │   │   │   │       ├── ModelNotInitializedException.java
    │   │   │   │       └── ResourceNotFoundException.java
    │   │   │   └── intelligentdecision/          # GROUP 4 BACKEND MODULE
    │   │   │       ├── config/
    │   │   │       │   └── DestinationDataLoader.java  # Sample Data Seeder
    │   │   │       ├── controller/
    │   │   │       │   └── DecisionController.java     # REST API Endpoints
    │   │   │       ├── dto/
    │   │   │       │   ├── DestinationRecommendation.java
    │   │   │       │   ├── DestinationRecommendationDTO.java
    │   │   │       │   ├── RecommendationRequest.java  # Input Payload Schema
    │   │   │       │   └── RecommendationResponse.java # Output Payload Schema
    │   │   │       ├── model/                        # JPA Entities & Enums
    │   │   │       │   ├── AgeGroup.java             # YOUNG_ADULT, ADULT, SENIOR
    │   │   │       │   ├── DecisionLog.java          # Inference Audit Logs
    │   │   │       │   ├── Destination.java          # Travel Destinations
    │   │   │       │   ├── SuitabilityLabel.java     # EXCELLENT, SUITABLE, MODERATE, UNSUITABLE
    │   │   │       │   ├── TravelHistory.java        # Historical Ratings
    │   │   │       │   ├── TravelerProfile.java      # User Profiles & Preferences
    │   │   │       │   └── TravelStyle.java          # SOLO, COUPLE, FAMILY, etc.
    │   │   │       ├── repository/                   # Spring Data JPA Repositories
    │   │   │       │   ├── DecisionLogRepository.java
    │   │   │       │   ├── DestinationRepository.java
    │   │   │       │   ├── TravelHistoryRepository.java
    │   │   │       │   └── TravelerProfileRepository.java
    │   │   │       └── service/                      # Core Logic & ML Engines
    │   │   │           ├── DecisionTreeService.java
    │   │   │           ├── DecisionTreeServiceImpl.java
    │   │   │           ├── KnnService.java
    │   │   │           ├── KnnServiceImpl.java
    │   │   │           ├── RankingService.java
    │   │   │           ├── RankingServiceImpl.java
    │   │   │           ├── RecommendationService.java
    │   │   │           ├── RecommendationServiceImpl.java
    │   │   │           ├── ml/
    │   │   │           │   ├── knn/                  # Cosine Distance & K-NN Index
    │   │   │           │   │   ├── DestinationRatingRecord.java
    │   │   │           │   │   ├── HistoricalTravelerIndexItem.java
    │   │   │           │   │   ├── KnnRecommendationResult.java
    │   │   │           │   │   └── NeighborMatch.java
    │   │   │           │   └── tree/                 # Decision Tree Data Structure
    │   │   │           │       ├── DecisionTreeClassifier.java
    │   │   │       │       ├── DecisionTreeNode.java
    │   │   │       │       └── DecisionTreePrediction.java
    │   │   │           └── preprocessing/            # Feature Vectors & Normalization
    │   │   │               ├── DataPreprocessor.java
    │   │   │               └── TravelerFeatureRecord.java
    │   │   └── resources/
    │   │       ├── application.properties        # Supabase PostgreSQL Configuration
    │   │       ├── application-local.properties  # In-Memory H2 Development Profile
    │   │       └── db/migration/                 # Flyway Database Schemas
    │   │           ├── V4__create_intelligent_decision_schema.sql
    │   │           └── V4_1__seed_destinations_and_sample_history.sql
    │   └── test/                                 # Unit & Integration Tests
    │       └── java/com/nibm/intelligenttravelmanagementsystem/intelligentdecision/
    │           ├── controller/
    │           ├── dto/
    │           ├── model/
    │           ├── repository/
    │           ├── service/
    │           └── Module4ApiIntegrationTest.java
```

---

## 3. How the Algorithms & Code Work

The decision engine operates through a **3-Tiered Hybrid ML Recommendation Pipeline**:

```
[ Traveler Request ] 
         │
         ▼
 1. DataPreprocessor ──► Normalizes Preferences & Scales Features
         │
         ├──► 2. DecisionTreeClassifier ──► Classifies Destination Suitability
         │                                  (EXCELLENT / SUITABLE / MODERATE / UNSUITABLE)
         │
         ├──► 3. KnnServiceImpl ─────────► Computes Cosine Similarity across
         │                                  Historical Traveler Feature Vectors (K=5)
         │
         ▼
 4. Multi-Criteria RankingEngine ───────► Aggregates Weighted Score:
         │                                 Score = (0.35 * DT_Prob) 
         │                                       + (0.25 * KNN_Consensus)
         │                                       + (0.30 * Pref_Match)
         │                                       - (0.10 * Budget_Penalty)
         ▼
 [ Top-N Ranked Destination Recommendations Response ]
```

### A. Data Preprocessing (`DataPreprocessor.java`)
- Extracts raw user preferences (Beach, Adventure, Nature, Culture, Nightlife, Relaxation) scale $[1, 10]$.
- Normalizes preference values into normalized vectors in $[0, 1]$.
- Scales numerical features (Budget per day, Group Size, Duration) using Min-Max scaling.

### B. Decision Tree Classifier (`DecisionTreeClassifier.java`, `DecisionTreeNode.java`)
- **Data Structure**: Binary Decision Tree recursively built using Information Gain / Gini Impurity split logic.
- **Node Evaluation**: Evaluates whether a destination's category score matches the traveler's dominant style.
- **Prediction**: Returns a `DecisionTreePrediction` containing the predicted `SuitabilityLabel` (`EXCELLENT`, `SUITABLE`, `MODERATE`, `UNSUITABLE`) and probability score $P(\text{Suitability} \ge \text{SUITABLE})$.

### C. K-Nearest Neighbors (KNN) (`KnnServiceImpl.java`, `NeighborMatch.java`)
- Maintains an in-memory spatial index of historical traveler profiles.
- Uses **Cosine Similarity** between preference vectors $\mathbf{u}$ and $\mathbf{v}$:
  $$\text{Similarity}(\mathbf{u}, \mathbf{v}) = \frac{\mathbf{u} \cdot \mathbf{v}}{\|\mathbf{u}\| \|\mathbf{v}\|}$$
- Finds the top $K=5$ nearest historical neighbors to identify destinations highly rated by similar travelers.

### D. Multi-Criteria Ranking Engine (`RankingServiceImpl.java`)
- Combines model outputs into a final ranking score $S(d)$ for destination $d$:
  $$S(d) = w_1 \cdot P_{\text{Tree}}(d) + w_2 \cdot C_{\text{KNN}}(d) + w_3 \cdot \text{PrefMatch}(d) - w_4 \cdot \text{ConstraintPenalty}(d)$$
- **Default Weights**:
  - $w_1 = 0.35$ (Decision Tree Probability)
  - $w_2 = 0.25$ (KNN Neighbor Consensus)
  - $w_3 = 0.30$ (Direct Category Preference Alignment)
  - $w_4 = 0.10$ (Budget Overrun / Duration Discrepancy Penalty)
- Sorts candidates in descending order and returns the Top-$N$ recommendations with match percentages and breakdown rationale.

---

## 4. Supabase Database Integration

The system connects to **Supabase PostgreSQL** via standard Spring Data JPA and Flyway migration scripts.

### Database Credentials (`application.properties`)
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres.yfiqrjkhfcjypzxracom}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:rbEm43GGcL3JdtFd}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

### Database Tables (Flyway Schema `V4__...`)
1. `destinations`: Stores destination attributes (category ratings 1-10, cost per day, location coordinates, climate).
2. `traveler_profiles`: Stores traveler demographic details, travel style, and preference ratings.
3. `travel_history`: Stores past visit logs, traveler ratings, and feedback.
4. `decision_logs`: Stores ML prediction audit trail, generated recommendations, and confidence metrics.

---

## 5. REST API Reference & Postman Testing

### Endpoints Overview

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/decisions/health` | Subsystem operational status health check |
| `POST` | `/api/decisions/recommend` | Generate top-N personalized destination recommendations |

---

### Request Payload Example (`POST /api/decisions/recommend`)

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

### Response Payload Example

```json
{
  "success": true,
  "message": "Recommendations generated successfully",
  "data": {
    "recommendationId": "9f3a12b4-5678-4321-9876-123456789abc",
    "totalCandidatesEvaluated": 15,
    "topN": 5,
    "recommendations": [
      {
        "destinationId": "a1b2c3d4-0000-1111-2222-333344445555",
        "destinationName": "Mirissa Coast",
        "region": "Southern Province",
        "overallMatchScore": 0.942,
        "matchPercentage": 94.2,
        "decisionTreeSuitability": "EXCELLENT",
        "knnConsensusScore": 0.88,
        "preferenceMatchScore": 0.96,
        "estimatedCostPerPerson": 450.00,
        "matchRationale": "Strong alignment with beach (9/10) and relaxation (9/10) preferences."
      }
    ]
  }
}
```

---

## 6. How to Run & Verify

### Running Unit & Integration Tests
```powershell
cd Intelligent-Travel-Management-System
.\mvnw.cmd test
```

### Starting the Spring Boot Backend Locally
```powershell
cd Intelligent-Travel-Management-System
.\mvnw.cmd spring-boot:run
```
The server will start on port `8085`. Access the health check at:
`http://localhost:8085/api/decisions/health`

### Postman Testing
1. Launch **Postman**.
2. Click **Import** and select `Group4_PDSA_Backend.postman_collection.json`.
3. Select any request (e.g. `2. Get Travel Recommendations`) and hit **Send**.
