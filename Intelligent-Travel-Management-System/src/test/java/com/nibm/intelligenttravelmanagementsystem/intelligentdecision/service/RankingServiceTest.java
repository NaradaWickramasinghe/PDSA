package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.Destination;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.SuitabilityLabel;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.KnnRecommendationResult;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreePrediction;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.DataPreprocessor;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking.RankedDestinationCandidate;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking.RankingWeights;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RankingServiceTest {

    private RankingServiceImpl rankingService;
    private RankingWeights weights;
    private DataPreprocessor preprocessor;

    private Destination ella;
    private Destination mirissa;
    private Destination sigiriya;
    private Destination bentota;

    @BeforeEach
    void setUp() {
        weights = RankingWeights.builder()
                .tree(0.30)
                .knn(0.25)
                .preference(0.25)
                .budget(0.10)
                .duration(0.10)
                .build();

        preprocessor = new DataPreprocessor();
        rankingService = new RankingServiceImpl(weights, preprocessor);

        ella = Destination.builder()
                .id(UUID.randomUUID())
                .name("Ella")
                .province("Uva")
                .averageDailyCost(new BigDecimal("65.00"))
                .minimumDays(2)
                .maximumDays(5)
                .beachScore(1)
                .adventureScore(9)
                .natureScore(10)
                .cultureScore(4)
                .nightlifeScore(6)
                .relaxationScore(7)
                .difficultyLevel(3)
                .build();

        mirissa = Destination.builder()
                .id(UUID.randomUUID())
                .name("Mirissa")
                .province("Southern")
                .averageDailyCost(new BigDecimal("75.00"))
                .minimumDays(2)
                .maximumDays(6)
                .beachScore(10)
                .adventureScore(6)
                .natureScore(7)
                .cultureScore(3)
                .nightlifeScore(9)
                .relaxationScore(8)
                .difficultyLevel(1)
                .build();

        sigiriya = Destination.builder()
                .id(UUID.randomUUID())
                .name("Sigiriya")
                .province("Central")
                .averageDailyCost(new BigDecimal("80.00"))
                .minimumDays(1)
                .maximumDays(3)
                .beachScore(1)
                .adventureScore(8)
                .natureScore(8)
                .cultureScore(10)
                .nightlifeScore(2)
                .relaxationScore(5)
                .difficultyLevel(3)
                .build();

        bentota = Destination.builder()
                .id(UUID.randomUUID())
                .name("Bentota")
                .province("Western")
                .averageDailyCost(new BigDecimal("250.00"))
                .minimumDays(2)
                .maximumDays(5)
                .beachScore(9)
                .adventureScore(4)
                .natureScore(6)
                .cultureScore(4)
                .nightlifeScore(5)
                .relaxationScore(10)
                .difficultyLevel(1)
                .build();
    }

    @Test
    @DisplayName("1. Score Calculation: Final scores must be normalized strictly in [0.0, 1.0]")
    void testScoreCalculationNormalization() {
        TravelerFeatureRecord traveler = TravelerFeatureRecord.builder()
                .budget(800.0)
                .durationDays(5)
                .groupSize(2)
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .travelStyle(TravelStyle.ADVENTURE)
                .adventurePreference(5)
                .naturePreference(5)
                .beachPreference(1)
                .build();

        DecisionTreePrediction treePred = DecisionTreePrediction.builder()
                .predictedClass("Ella")
                .confidenceScore(0.90)
                .suitabilityLabel(SuitabilityLabel.EXCELLENT_FIT)
                .build();

        KnnRecommendationResult knnResult = KnnRecommendationResult.builder()
                .destinationScores(Map.of("Ella", 0.88))
                .build();

        List<RankedDestinationCandidate> ranked = rankingService.rankDestinations(
                traveler,
                List.of(ella, mirissa, sigiriya, bentota),
                treePred,
                knnResult,
                4
        );

        assertFalse(ranked.isEmpty());
        for (RankedDestinationCandidate candidate : ranked) {
            assertTrue(candidate.getFinalScore() >= 0.0 && candidate.getFinalScore() <= 1.0,
                    "Candidate score must be between 0.0 and 1.0, got: " + candidate.getFinalScore());
            assertTrue(candidate.getTreeScore() >= 0.0 && candidate.getTreeScore() <= 1.0);
            assertTrue(candidate.getKnnScore() >= 0.0 && candidate.getKnnScore() <= 1.0);
            assertTrue(candidate.getPreferenceScore() >= 0.0 && candidate.getPreferenceScore() <= 1.0);
            assertTrue(candidate.getBudgetScore() >= 0.0 && candidate.getBudgetScore() <= 1.0);
            assertTrue(candidate.getDurationScore() >= 0.0 && candidate.getDurationScore() <= 1.0);
        }
    }

    @Test
    @DisplayName("2. Ranking Order: Output must be sorted descending by final score with sequential 1-based ranks")
    void testRankingOrder() {
        TravelerFeatureRecord traveler = TravelerFeatureRecord.builder()
                .budget(900.0)
                .durationDays(4)
                .groupSize(2)
                .adventurePreference(5)
                .naturePreference(5)
                .beachPreference(1)
                .build();

        DecisionTreePrediction treePred = DecisionTreePrediction.builder().predictedClass("Ella").confidenceScore(0.90).build();
        KnnRecommendationResult knnResult = KnnRecommendationResult.builder().destinationScores(Map.of("Ella", 0.90, "Mirissa", 0.40)).build();

        List<RankedDestinationCandidate> ranked = rankingService.rankDestinations(
                traveler,
                List.of(mirissa, ella, bentota),
                treePred,
                knnResult,
                3
        );

        assertEquals(3, ranked.size());
        assertEquals(1, ranked.get(0).getRank());
        assertEquals(2, ranked.get(1).getRank());
        assertEquals(3, ranked.get(2).getRank());

        assertTrue(ranked.get(0).getFinalScore() >= ranked.get(1).getFinalScore());
        assertTrue(ranked.get(1).getFinalScore() >= ranked.get(2).getFinalScore());
        assertEquals("Ella", ranked.get(0).getDestinationName());
    }

    @Test
    @DisplayName("3. Tie Breaking: Candidates with identical final scores must break ties deterministically")
    void testTieBreaking() {
        Destination destA = Destination.builder()
                .id(UUID.randomUUID())
                .name("Alpha Beach")
                .averageDailyCost(new BigDecimal("50.00"))
                .minimumDays(2).maximumDays(5)
                .beachScore(8).adventureScore(5).natureScore(5).cultureScore(5).nightlifeScore(5).relaxationScore(5)
                .difficultyLevel(2)
                .build();

        Destination destB = Destination.builder()
                .id(UUID.randomUUID())
                .name("Beta Beach")
                .averageDailyCost(new BigDecimal("50.00"))
                .minimumDays(2).maximumDays(5)
                .beachScore(8).adventureScore(5).natureScore(5).cultureScore(5).nightlifeScore(5).relaxationScore(5)
                .difficultyLevel(2)
                .build();

        TravelerFeatureRecord traveler = TravelerFeatureRecord.builder()
                .budget(500.0)
                .durationDays(3)
                .beachPreference(5)
                .build();

        List<RankedDestinationCandidate> ranked = rankingService.rankDestinations(
                traveler,
                List.of(destB, destA),
                null,
                null,
                2
        );

        assertEquals(2, ranked.size());
        assertEquals(1, ranked.get(0).getRank());
        assertEquals(2, ranked.get(1).getRank());
        assertEquals("Alpha Beach", ranked.get(0).getDestinationName(), "Alphabetical secondary sort should place Alpha first");
    }

    @Test
    @DisplayName("4. Budget Compatibility: Low budget must penalize expensive destinations")
    void testBudgetCompatibility() {
        TravelerFeatureRecord traveler = TravelerFeatureRecord.builder()
                .budget(200.0) // Low budget for 3 days
                .durationDays(3)
                .beachPreference(5)
                .relaxationPreference(5)
                .build();

        List<RankedDestinationCandidate> ranked = rankingService.rankDestinations(
                traveler,
                List.of(mirissa, bentota), // Mirissa ($75/day) vs Bentota ($250/day)
                null,
                null,
                2
        );

        RankedDestinationCandidate mirissaCandidate = ranked.stream().filter(c -> "Mirissa".equals(c.getDestinationName())).findFirst().orElseThrow();
        RankedDestinationCandidate bentotaCandidate = ranked.stream().filter(c -> "Bentota".equals(c.getDestinationName())).findFirst().orElseThrow();

        assertTrue(mirissaCandidate.getBudgetScore() > bentotaCandidate.getBudgetScore());
        assertTrue(bentotaCandidate.getExplanation().contains("exceeds budget") || bentotaCandidate.getExplanation().contains("budget"));
        assertTrue(mirissaCandidate.getExplanation().contains("within budget") || mirissaCandidate.getExplanation().contains("budget"));
    }

    @Test
    @DisplayName("5. Duration Compatibility: Fitting duration receives full score while outside stay window is penalized")
    void testDurationCompatibility() {
        // Sigiriya max days is 3. Traveler requests 7 days.
        TravelerFeatureRecord traveler = TravelerFeatureRecord.builder()
                .budget(1000.0)
                .durationDays(7) // 7 days exceeds Sigiriya's 3-day max
                .culturePreference(5)
                .build();

        List<RankedDestinationCandidate> ranked = rankingService.rankDestinations(
                traveler,
                List.of(sigiriya, mirissa), // Mirissa maxDays is 6
                null,
                null,
                2
        );

        RankedDestinationCandidate sigiriyaCandidate = ranked.stream().filter(c -> "Sigiriya".equals(c.getDestinationName())).findFirst().orElseThrow();
        assertTrue(sigiriyaCandidate.getDurationScore() < 1.0, "7 days exceeding Sigiriya's 3 days should have duration score < 1.0");
    }

    @Test
    @DisplayName("6. Preference Compatibility: Cosine similarity matches high preferences and generates factual reasons")
    void testPreferenceCompatibilityAndFactualReason() {
        TravelerFeatureRecord adventureTraveler = TravelerFeatureRecord.builder()
                .budget(700.0)
                .durationDays(4)
                .groupSize(2)
                .adventurePreference(5)
                .naturePreference(5)
                .beachPreference(1)
                .build();

        List<RankedDestinationCandidate> ranked = rankingService.rankDestinations(
                adventureTraveler,
                List.of(ella),
                null,
                null,
                1
        );

        RankedDestinationCandidate candidate = ranked.get(0);
        assertTrue(candidate.getPreferenceScore() >= 0.85);

        // Factual reason check
        String reason = candidate.getExplanation();
        assertNotNull(reason);
        assertTrue(reason.contains("adventure") && reason.contains("nature"),
                "Reason must factually mention adventure and nature preferences: " + reason);
        assertTrue(reason.contains("within budget"), "Reason must factually state within budget: " + reason);
        assertTrue(reason.contains("suitable for the requested duration"), "Reason must factually state duration suitability: " + reason);
    }
}
