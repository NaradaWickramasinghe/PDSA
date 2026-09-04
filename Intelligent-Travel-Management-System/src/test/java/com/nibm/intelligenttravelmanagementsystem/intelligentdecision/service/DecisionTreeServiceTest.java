package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelerProfile;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreePrediction;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.DataPreprocessor;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DecisionTreeServiceTest {

    private DecisionTreeServiceImpl decisionTreeService;
    private DataPreprocessor preprocessor;

    @BeforeEach
    void setUp() {
        preprocessor = new DataPreprocessor();
        decisionTreeService = new DecisionTreeServiceImpl(preprocessor);
        decisionTreeService.init(); // Train the CART model on dataset
    }

    @Test
    @DisplayName("Model should be ready and loaded after initialization")
    void testModelInitialization() {
        assertTrue(decisionTreeService.isModelReady(), "Decision tree model should be ready");
    }

    @Test
    @DisplayName("Adventure-focused traveler should predict adventure destination category")
    void testAdventureFocusedTraveler() {
        TravelerFeatureRecord adventureTraveler = TravelerFeatureRecord.builder()
                .budget(600.0)
                .durationDays(4)
                .groupSize(2)
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .travelStyle(TravelStyle.ADVENTURE)
                .beachPreference(1)
                .adventurePreference(5)
                .naturePreference(5)
                .culturePreference(2)
                .nightlifePreference(2)
                .relaxationPreference(2)
                .build();

        DecisionTreePrediction prediction = decisionTreeService.predict(adventureTraveler);

        assertNotNull(prediction);
        assertNotNull(prediction.getPredictedClass());
        assertTrue(prediction.getConfidenceScore() > 0.0);
        assertTrue(prediction.getPredictedClass().equals("Ella") || prediction.getPredictedClass().equals("Knuckles Mountain Range"),
                "Adventure persona should classify into Ella or Knuckles, got: " + prediction.getPredictedClass());
        assertFalse(prediction.getDecisionPathRules().isEmpty(), "Should contain decision path rules");
    }

    @Test
    @DisplayName("Beach-focused traveler should classify into coastal destination")
    void testBeachFocusedTraveler() {
        TravelerFeatureRecord beachTraveler = TravelerFeatureRecord.builder()
                .budget(800.0)
                .durationDays(5)
                .groupSize(4)
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .travelStyle(TravelStyle.FRIENDS)
                .beachPreference(5)
                .adventurePreference(3)
                .naturePreference(3)
                .culturePreference(1)
                .nightlifePreference(5)
                .relaxationPreference(4)
                .build();

        DecisionTreePrediction prediction = decisionTreeService.predict(beachTraveler);

        assertNotNull(prediction);
        assertTrue(prediction.getPredictedClass().equals("Mirissa") ||
                        prediction.getPredictedClass().equals("Arugam Bay") ||
                        prediction.getPredictedClass().equals("Trincomalee") ||
                        prediction.getPredictedClass().equals("Bentota"),
                "Beach persona should classify into a coastal destination, got: " + prediction.getPredictedClass());
    }

    @Test
    @DisplayName("Nature and wildlife-focused traveler should classify into wildlife/nature destination")
    void testNatureFocusedTraveler() {
        TravelerFeatureRecord natureTraveler = TravelerFeatureRecord.builder()
                .budget(1000.0)
                .durationDays(3)
                .groupSize(2)
                .ageGroup(AgeGroup.ADULT)
                .travelStyle(TravelStyle.ADVENTURE)
                .beachPreference(2)
                .adventurePreference(4)
                .naturePreference(5)
                .culturePreference(1)
                .nightlifePreference(1)
                .relaxationPreference(2)
                .build();

        DecisionTreePrediction prediction = decisionTreeService.predict(natureTraveler);

        assertNotNull(prediction);
        assertTrue(prediction.getPredictedClass().equals("Yala National Park") ||
                        prediction.getPredictedClass().equals("Ella") ||
                        prediction.getPredictedClass().equals("Nuwara Eliya"),
                "Nature persona should classify into Yala/Ella/Nuwara Eliya, got: " + prediction.getPredictedClass());
    }

    @Test
    @DisplayName("Low-budget traveler with cultural interest should be handled cleanly")
    void testLowBudgetTraveler() {
        TravelerFeatureRecord budgetTraveler = TravelerFeatureRecord.builder()
                .budget(350.0)
                .durationDays(2)
                .groupSize(2)
                .ageGroup(AgeGroup.SENIOR)
                .travelStyle(TravelStyle.COUPLE)
                .beachPreference(1)
                .adventurePreference(1)
                .naturePreference(2)
                .culturePreference(5)
                .nightlifePreference(1)
                .relaxationPreference(4)
                .build();

        DecisionTreePrediction prediction = decisionTreeService.predict(budgetTraveler);

        assertNotNull(prediction);
        assertTrue(prediction.getPredictedClass().equals("Anuradhapura") ||
                        prediction.getPredictedClass().equals("Kandy") ||
                        prediction.getPredictedClass().equals("Sigiriya"),
                "Budget cultural persona should classify into heritage site, got: " + prediction.getPredictedClass());
    }

    @Test
    @DisplayName("Different trip durations (short 2-day vs extended 7-day) should produce structured predictions")
    void testDifferentDurations() {
        // Short 2-day trip
        TravelerFeatureRecord shortTrip = TravelerFeatureRecord.builder()
                .budget(400.0)
                .durationDays(2)
                .groupSize(2)
                .naturePreference(4)
                .culturePreference(5)
                .build();

        DecisionTreePrediction predShort = decisionTreeService.predict(shortTrip);
        assertNotNull(predShort);
        assertNotNull(predShort.getPredictedClass());

        // Extended 7-day trip
        TravelerFeatureRecord longTrip = TravelerFeatureRecord.builder()
                .budget(1500.0)
                .durationDays(7)
                .groupSize(2)
                .beachPreference(5)
                .relaxationPreference(5)
                .build();

        DecisionTreePrediction predLong = decisionTreeService.predict(longTrip);
        assertNotNull(predLong);
        assertNotNull(predLong.getPredictedClass());
        assertTrue(predLong.getPredictedClass().equals("Bentota") || predLong.getPredictedClass().equals("Mirissa") || predLong.getPredictedClass().equals("Arugam Bay") || predLong.getPredictedClass().equals("Trincomalee"));
    }

    @Test
    @DisplayName("Prediction directly from JPA TravelerProfile entity")
    void testPredictFromEntity() {
        TravelerProfile profile = TravelerProfile.builder()
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .budget(new BigDecimal("700.00"))
                .durationDays(5)
                .groupSize(2)
                .travelStyle(TravelStyle.ADVENTURE)
                .beachPreference(2)
                .adventurePreference(5)
                .naturePreference(5)
                .culturePreference(2)
                .nightlifePreference(3)
                .relaxationPreference(3)
                .build();

        DecisionTreePrediction prediction = decisionTreeService.predict(profile);
        assertNotNull(prediction);
        assertNotNull(prediction.getSuitabilityLabel());
        assertTrue(prediction.getConfidenceScore() >= 0.0);
    }

    @Test
    @DisplayName("Invalid or null input should be sanitized and processed without crashing")
    void testInvalidInputHandling() {
        TravelerFeatureRecord invalidInput = TravelerFeatureRecord.builder()
                .budget(-999.0)
                .durationDays(-5)
                .groupSize(0)
                .beachPreference(-10)
                .adventurePreference(100)
                .build();

        DecisionTreePrediction prediction = decisionTreeService.predict(invalidInput);
        assertNotNull(prediction, "Should return valid prediction even with invalid raw inputs");
        assertNotNull(prediction.getPredictedClass());
    }
}
