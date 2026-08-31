package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataPreprocessorTest {

    private DataPreprocessor preprocessor;

    @BeforeEach
    void setUp() {
        preprocessor = new DataPreprocessor();
    }

    @Test
    @DisplayName("Valid traveler profile should be cleaned and preserved accurately")
    void testValidDataCleaning() {
        TravelerFeatureRecord input = TravelerFeatureRecord.builder()
                .budget(800.0)
                .durationDays(5)
                .groupSize(2)
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .travelStyle(TravelStyle.ADVENTURE)
                .beachPreference(3)
                .adventurePreference(5)
                .naturePreference(4)
                .culturePreference(2)
                .nightlifePreference(4)
                .relaxationPreference(3)
                .targetDestination("Ella")
                .build();

        TravelerFeatureRecord cleaned = preprocessor.cleanAndImpute(input);

        assertEquals(800.0, cleaned.getBudget(), 0.001);
        assertEquals(5, cleaned.getDurationDays());
        assertEquals(2, cleaned.getGroupSize());
        assertEquals(AgeGroup.YOUNG_ADULT, cleaned.getAgeGroup());
        assertEquals(TravelStyle.ADVENTURE, cleaned.getTravelStyle());
        assertEquals(5, cleaned.getAdventurePreference());
    }

    @Test
    @DisplayName("Invalid data such as negative numbers and out-of-range scores should be clamped")
    void testInvalidDataClamping() {
        TravelerFeatureRecord input = TravelerFeatureRecord.builder()
                .budget(-500.0) // Invalid
                .durationDays(-3) // Invalid
                .groupSize(0) // Invalid
                .beachPreference(-2) // Invalid
                .adventurePreference(10) // Invalid > 5
                .naturePreference(0)
                .culturePreference(7)
                .nightlifePreference(3)
                .relaxationPreference(3)
                .build();

        TravelerFeatureRecord cleaned = preprocessor.cleanAndImpute(input);

        assertTrue(cleaned.getBudget() >= DataPreprocessor.MIN_BUDGET, "Budget should be clamped to valid baseline");
        assertEquals(4, cleaned.getDurationDays(), "Invalid duration should default to median 4 days");
        assertEquals(2, cleaned.getGroupSize(), "Invalid group size should default to 2");
        assertEquals(3, cleaned.getBeachPreference(), "Zero/negative preference should default to median 3");
        assertEquals(5, cleaned.getAdventurePreference(), "Score > 5 should be clamped to max 5");
    }

    @Test
    @DisplayName("Missing null values should receive sensible default imputations")
    void testMissingValuesImputation() {
        TravelerFeatureRecord input = TravelerFeatureRecord.builder()
                .budget(0.0)
                .durationDays(0)
                .groupSize(0)
                .ageGroup(null)
                .travelStyle(null)
                .beachPreference(0)
                .adventurePreference(0)
                .naturePreference(0)
                .culturePreference(0)
                .nightlifePreference(0)
                .relaxationPreference(0)
                .build();

        TravelerFeatureRecord cleaned = preprocessor.cleanAndImpute(input);

        assertEquals(800.0, cleaned.getBudget());
        assertEquals(4, cleaned.getDurationDays());
        assertEquals(2, cleaned.getGroupSize());
        assertEquals(AgeGroup.YOUNG_ADULT, cleaned.getAgeGroup());
        assertEquals(TravelStyle.COUPLE, cleaned.getTravelStyle(), "Group size 2 should infer COUPLE travel style");
        assertEquals(3, cleaned.getBeachPreference());
        assertEquals(3, cleaned.getAdventurePreference());
    }

    @Test
    @DisplayName("Min-Max normalization formula produces exact bounds in [0.0, 1.0]")
    void testNormalizationBounds() {
        assertEquals(0.0, preprocessor.normalize(100.0, 100.0, 3000.0), 0.0001);
        assertEquals(1.0, preprocessor.normalize(3000.0, 100.0, 3000.0), 0.0001);
        assertEquals(0.5, preprocessor.normalize(1550.0, 100.0, 3000.0), 0.0001);

        // Preference normalization (1 to 5)
        assertEquals(0.0, preprocessor.normalize(1.0, 1.0, 5.0), 0.0001);
        assertEquals(0.5, preprocessor.normalize(3.0, 1.0, 5.0), 0.0001);
        assertEquals(1.0, preprocessor.normalize(5.0, 1.0, 5.0), 0.0001);
    }

    @Test
    @DisplayName("Prediction-time feature extraction matches fixed training bounds and dimension layout")
    void testKnnVectorExtractionConsistency() {
        TravelerFeatureRecord record = TravelerFeatureRecord.builder()
                .budget(1550.0)
                .durationDays(14)
                .groupSize(1)
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .travelStyle(TravelStyle.ADVENTURE)
                .beachPreference(1)
                .adventurePreference(5)
                .naturePreference(5)
                .culturePreference(1)
                .nightlifePreference(3)
                .relaxationPreference(2)
                .build();

        double[] vector = preprocessor.extractKnnFeatures(record);

        assertEquals(DataPreprocessor.KNN_FEATURE_DIMENSION, vector.length, "Vector dimension must be exactly 20");
        assertEquals(0.5, vector[0], 0.001, "Budget 1550 normalized to 0.5");
        assertEquals(1.0, vector[1], 0.001, "Duration 14 normalized to 1.0");
        assertEquals(0.0, vector[2], 0.001, "Group size 1 normalized to 0.0");
        assertEquals(0.0, vector[3], 0.001, "Beach preference 1 normalized to 0.0");
        assertEquals(1.0, vector[4], 0.001, "Adventure preference 5 normalized to 1.0");

        // One-hot tests
        assertEquals(0.0, vector[9], "TEEN one-hot should be 0");
        assertEquals(1.0, vector[10], "YOUNG_ADULT one-hot should be 1");
        assertEquals(1.0, vector[17], "ADVENTURE travel style one-hot should be 1");
        assertEquals(0.0, vector[18], "LUXURY travel style one-hot should be 0");
    }

    @Test
    @DisplayName("Synthetic dataset generation produces 600 balanced, non-empty records")
    void testSyntheticDatasetGeneration() {
        List<TravelerFeatureRecord> dataset = preprocessor.generateSyntheticDataset(42L);

        assertEquals(600, dataset.size(), "Dataset must contain 600 samples");
        long ellaCount = dataset.stream().filter(r -> "Ella".equals(r.getTargetDestination())).count();
        assertEquals(50, ellaCount, "Each destination must have exactly 50 samples");

        for (TravelerFeatureRecord record : dataset) {
            assertNotNull(record.getTargetDestination());
            assertTrue(record.getBudget() >= 100.0);
            assertTrue(record.getDurationDays() >= 1);
            assertTrue(record.getAdventurePreference() >= 1 && record.getAdventurePreference() <= 5);
        }
    }
}
