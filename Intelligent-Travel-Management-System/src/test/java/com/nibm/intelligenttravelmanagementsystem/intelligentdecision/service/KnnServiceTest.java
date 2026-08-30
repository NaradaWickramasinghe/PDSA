package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.KnnRecommendationResult;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.DataPreprocessor;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnnServiceTest {

    private KnnServiceImpl knnService;
    private DataPreprocessor preprocessor;

    @BeforeEach
    void setUp() {
        preprocessor = new DataPreprocessor();
        knnService = new KnnServiceImpl(preprocessor);
        knnService.init(); // Loads 600 indexed historical samples
    }

    @Test
    @DisplayName("KnnService should have indexed baseline historical travelers")
    void testInitialization() {
        assertEquals(600, knnService.getIndexedTravelerCount());
    }

    @Test
    @DisplayName("Nearest travelers: Discovers closest peers and computes Euclidean distances")
    void testNearestNeighborsDiscovery() {
        TravelerFeatureRecord newTraveler = TravelerFeatureRecord.builder()
                .budget(500.0)
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

        KnnRecommendationResult result = knnService.findRecommendations(newTraveler, 5);

        assertNotNull(result);
        assertEquals(5, result.getNearestNeighbors().size());
        for (int i = 0; i < result.getNearestNeighbors().size() - 1; i++) {
            assertTrue(result.getNearestNeighbors().get(i).getDistance() <=
                            result.getNearestNeighbors().get(i + 1).getDistance(),
                    "Neighbors must be sorted ascending by distance");
        }
    }

    @Test
    @DisplayName("Destination voting: Aggregates inverse-distance weighted ratings from peer neighbors")
    void testDestinationVotingAggregation() {
        TravelerFeatureRecord traveler = TravelerFeatureRecord.builder()
                .budget(600.0)
                .durationDays(4)
                .groupSize(2)
                .adventurePreference(5)
                .naturePreference(5)
                .build();

        KnnRecommendationResult result = knnService.findRecommendations(traveler, 5);

        assertNotNull(result);
        assertFalse(result.getTopEvidences().isEmpty());
        for (KnnRecommendationResult.KnnDestinationEvidence evidence : result.getTopEvidences()) {
            assertTrue(evidence.getVoteCount() >= 1);
            assertTrue(evidence.getAverageRating() >= 1.0 && evidence.getAverageRating() <= 5.0);
            assertTrue(evidence.getEvidenceScore() >= 0.0 && evidence.getEvidenceScore() <= 1.0);
            assertNotNull(evidence.getExplanation());
        }
    }

    @Test
    @DisplayName("Beach traveler should receive coastal destinations as top evidence")
    void testBeachTravelerRecommendation() {
        TravelerFeatureRecord beachTraveler = TravelerFeatureRecord.builder()
                .budget(750.0)
                .durationDays(5)
                .groupSize(3)
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .travelStyle(TravelStyle.FRIENDS)
                .beachPreference(5)
                .adventurePreference(3)
                .naturePreference(3)
                .culturePreference(1)
                .nightlifePreference(5)
                .relaxationPreference(4)
                .build();

        KnnRecommendationResult result = knnService.findRecommendations(beachTraveler, 5);

        assertNotNull(result);
        String topDest = result.getTopEvidences().get(0).getDestinationName();
        assertTrue(List.of("Mirissa", "Arugam Bay", "Bentota", "Trincomalee").contains(topDest),
                "Top destination should be coastal, got: " + topDest);
    }

    @Test
    @DisplayName("Budget scale must not dominate distance calculation due to Min-Max normalization")
    void testBudgetNormalizationInDistance() {
        // Traveler A: Budget $500, Adv 5
        TravelerFeatureRecord travelerA = TravelerFeatureRecord.builder()
                .budget(500.0)
                .durationDays(3)
                .groupSize(2)
                .adventurePreference(5)
                .naturePreference(5)
                .beachPreference(1)
                .build();

        // Traveler B: Same traits, budget $1500 (high budget)
        TravelerFeatureRecord travelerB = TravelerFeatureRecord.builder()
                .budget(1500.0)
                .durationDays(3)
                .groupSize(2)
                .adventurePreference(5)
                .naturePreference(5)
                .beachPreference(1)
                .build();

        KnnRecommendationResult resA = knnService.findRecommendations(travelerA, 5);
        KnnRecommendationResult resB = knnService.findRecommendations(travelerB, 5);

        // In both cases, adventure destinations should emerge as dominant evidence
        assertTrue(resA.getDestinationScores().containsKey("Ella") || resA.getDestinationScores().containsKey("Knuckles Mountain Range"));
        assertTrue(resB.getDestinationScores().containsKey("Ella") || resB.getDestinationScores().containsKey("Knuckles Mountain Range") || resB.getDestinationScores().containsKey("Yala National Park"));
    }

    @Test
    @DisplayName("Evaluate across candidate K values: 1, 3, 5, 7, 9")
    void testCandidateKValues() {
        TravelerFeatureRecord testTraveler = TravelerFeatureRecord.builder()
                .budget(800.0)
                .durationDays(5)
                .groupSize(2)
                .ageGroup(AgeGroup.ADULT)
                .beachPreference(1)
                .adventurePreference(2)
                .naturePreference(5)
                .culturePreference(4)
                .nightlifePreference(1)
                .relaxationPreference(5)
                .build();

        int[] kCandidates = {1, 3, 5, 7, 9};

        for (int k : kCandidates) {
            KnnRecommendationResult result = knnService.findRecommendations(testTraveler, k);
            assertEquals(k, result.getKUsed());
            assertEquals(k, result.getNearestNeighbors().size());
            assertFalse(result.getDestinationScores().isEmpty());
        }
    }

    @Test
    @DisplayName("Cold-start handling when historical index is empty")
    void testColdStart() {
        knnService.loadHistoricalData(Collections.emptyList());
        assertEquals(0, knnService.getIndexedTravelerCount());

        TravelerFeatureRecord traveler = TravelerFeatureRecord.builder().budget(500.0).build();
        KnnRecommendationResult result = knnService.findRecommendations(traveler, 5);

        assertNotNull(result);
        assertEquals(0, result.getKUsed());
        assertTrue(result.getDestinationScores().isEmpty());
    }
}
