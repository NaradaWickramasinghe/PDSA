package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.DestinationRecommendation;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationRequest;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationResponse;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.Destination;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.SuitabilityLabel;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository.DecisionLogRepository;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository.DestinationRepository;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository.TravelerProfileRepository;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.KnnRecommendationResult;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreePrediction;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.DataPreprocessor;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking.RankedDestinationCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private TravelerProfileRepository travelerProfileRepository;

    @Mock
    private DecisionLogRepository decisionLogRepository;

    @Mock
    private DecisionTreeService decisionTreeService;

    @Mock
    private KnnService knnService;

    @Mock
    private RankingService rankingService;

    private DataPreprocessor preprocessor;
    private RecommendationServiceImpl recommendationService;

    private Destination ella;
    private Destination mirissa;

    @BeforeEach
    void setUp() {
        preprocessor = new DataPreprocessor();
        recommendationService = new RecommendationServiceImpl(
                destinationRepository,
                travelerProfileRepository,
                decisionLogRepository,
                preprocessor,
                decisionTreeService,
                knnService,
                rankingService
        );

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
    }

    @Test
    @DisplayName("RecommendationService orchestrates full pipeline and outputs DestinationRecommendation DTOs")
    void testFullOrchestrationPipeline() {
        RecommendationRequest request = RecommendationRequest.builder()
                .travelerId(UUID.randomUUID())
                .budget(new BigDecimal("800.00"))
                .durationDays(5)
                .groupSize(2)
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .travelStyle(TravelStyle.ADVENTURE)
                .beachPreference(3)
                .adventurePreference(9)
                .naturePreference(8)
                .culturePreference(6)
                .nightlifePreference(4)
                .relaxationPreference(5)
                .topN(2)
                .build();

        when(destinationRepository.findAll()).thenReturn(List.of(ella, mirissa));

        DecisionTreePrediction mockTreePred = DecisionTreePrediction.builder()
                .predictedClass("Ella")
                .confidenceScore(0.92)
                .suitabilityLabel(SuitabilityLabel.EXCELLENT_FIT)
                .build();
        when(decisionTreeService.predict(any(TravelerFeatureRecord.class))).thenReturn(mockTreePred);

        KnnRecommendationResult mockKnnResult = KnnRecommendationResult.builder()
                .destinationScores(Map.of("Ella", 0.88, "Mirissa", 0.40))
                .build();
        when(knnService.findRecommendations(any(TravelerFeatureRecord.class), eq(5))).thenReturn(mockKnnResult);

        RankedDestinationCandidate rankedElla = RankedDestinationCandidate.builder()
                .destinationId(ella.getId())
                .destinationName("Ella")
                .rank(1)
                .finalScore(0.92)
                .treeScore(0.90)
                .knnScore(0.88)
                .preferenceScore(0.95)
                .budgetScore(0.90)
                .durationScore(1.0)
                .suitabilityLabel(SuitabilityLabel.EXCELLENT_FIT)
                .explanation("Strong match for adventure and nature preferences, within budget and suitable for the requested duration.")
                .destination(ella)
                .build();

        RankedDestinationCandidate rankedMirissa = RankedDestinationCandidate.builder()
                .destinationId(mirissa.getId())
                .destinationName("Mirissa")
                .rank(2)
                .finalScore(0.72)
                .treeScore(0.40)
                .knnScore(0.40)
                .preferenceScore(0.65)
                .budgetScore(0.85)
                .durationScore(1.0)
                .suitabilityLabel(SuitabilityLabel.MODERATE_FIT)
                .explanation("Good coastal option, within budget and suitable for the requested duration.")
                .destination(mirissa)
                .build();

        when(rankingService.rankDestinations(
                any(TravelerFeatureRecord.class),
                eq(List.of(ella, mirissa)),
                eq(mockTreePred),
                eq(mockKnnResult),
                eq(2)
        )).thenReturn(List.of(rankedElla, rankedMirissa));

        RecommendationResponse response = recommendationService.getRecommendations(request);

        assertNotNull(response);
        assertEquals(request.getTravelerId(), response.getTravelerId());
        assertEquals(2, response.getRecommendations().size());
        assertEquals("Ella", response.getDecisionTreePrimaryPrediction());

        DestinationRecommendation top = response.getRecommendations().get(0);
        assertEquals(1, top.getRank());
        assertEquals("Ella", top.getDestination());
        assertEquals(0.92, top.getScore());
        assertEquals(92.0, top.getMatchPercentage());
        assertEquals(SuitabilityLabel.EXCELLENT_FIT, top.getSuitabilityLabel());
        assertTrue(top.getReason().contains("adventure and nature"));

        // Verify delegation interactions
        verify(destinationRepository, times(1)).findAll();
        verify(decisionTreeService, times(1)).predict(any(TravelerFeatureRecord.class));
        verify(knnService, times(1)).findRecommendations(any(TravelerFeatureRecord.class), eq(5));
        verify(rankingService, times(1)).rankDestinations(any(), any(), any(), any(), eq(2));
        verify(decisionLogRepository, times(1)).saveAll(anyList());
    }
}
