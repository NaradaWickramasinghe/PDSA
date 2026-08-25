package com.nibm.intelligenttravelmanagementsystem.routesequencing;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.DestinationRecommendation;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationRequest;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationResponse;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.SuitabilityLabel;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.RecommendationService;
import com.nibm.intelligenttravelmanagementsystem.routesequencing.dto.ItineraryDayStop;
import com.nibm.intelligenttravelmanagementsystem.routesequencing.dto.ItineraryPlanRequest;
import com.nibm.intelligenttravelmanagementsystem.routesequencing.dto.ItineraryPlanResponse;
import com.nibm.intelligenttravelmanagementsystem.routesequencing.service.RouteSequencingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Module4And5IntegrationTest {

    @Mock
    private RecommendationService recommendationService;

    private RouteSequencingServiceImpl routeSequencingService;

    @BeforeEach
    void setUp() {
        routeSequencingService = new RouteSequencingServiceImpl(recommendationService);
    }

    @Test
    @DisplayName("Integration: Module 5 consumes Module 4 recommendations to build a multi-stop itinerary")
    void testModule5ConsumesModule4Recommendations() {
        RecommendationRequest travelerReq = RecommendationRequest.builder()
                .budget(new BigDecimal("800.00"))
                .durationDays(5)
                .groupSize(2)
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .travelStyle(TravelStyle.ADVENTURE)
                .adventurePreference(9)
                .naturePreference(8)
                .beachPreference(3)
                .culturePreference(4)
                .nightlifePreference(3)
                .relaxationPreference(4)
                .topN(3)
                .build();

        ItineraryPlanRequest planReq = ItineraryPlanRequest.builder()
                .travelerProfile(travelerReq)
                .maxStops(2)
                .build();

        DestinationRecommendation recElla = DestinationRecommendation.builder()
                .destination("Ella")
                .province("Uva")
                .rank(1)
                .score(0.92)
                .averageDailyCost(new BigDecimal("65.00"))
                .minimumDays(3)
                .maximumDays(5)
                .suitabilityLabel(SuitabilityLabel.EXCELLENT_FIT)
                .reason("Strong match for adventure and nature preferences, within budget and suitable for the requested duration.")
                .destinationId(UUID.randomUUID())
                .build();

        DestinationRecommendation recKnuckles = DestinationRecommendation.builder()
                .destination("Knuckles Mountain Range")
                .province("Central")
                .rank(2)
                .score(0.85)
                .averageDailyCost(new BigDecimal("50.00"))
                .minimumDays(2)
                .maximumDays(4)
                .suitabilityLabel(SuitabilityLabel.EXCELLENT_FIT)
                .reason("Strong match for hiking and nature preferences, within budget.")
                .destinationId(UUID.randomUUID())
                .build();

        RecommendationResponse mockModule4Response = RecommendationResponse.builder()
                .totalCandidatesEvaluated(12)
                .recommendations(List.of(recElla, recKnuckles))
                .decisionTreePrimaryPrediction("Ella")
                .summaryRationale("Top destination is Ella (Score: 92%).")
                .generatedAt(OffsetDateTime.now())
                .build();

        when(recommendationService.getRecommendations(any(RecommendationRequest.class)))
                .thenReturn(mockModule4Response);

        // Execute Module 5 Itinerary Sequencing
        ItineraryPlanResponse itinerary = routeSequencingService.createPersonalizedItinerary(planReq);

        assertNotNull(itinerary);
        assertEquals(5, itinerary.getTotalDays()); // 3 days Ella + 2 days Knuckles = 5 days total
        assertEquals(2, itinerary.getTotalDestinationsCount());
        assertEquals(new BigDecimal("295.00"), itinerary.getTotalEstimatedCost()); // ($65*3) + ($50*2) = 195 + 100 = 295

        List<ItineraryDayStop> stops = itinerary.getSequencedStops();
        assertEquals(2, stops.size());

        // Stop 1
        assertEquals(1, stops.get(0).getDaySequence());
        assertEquals("Ella", stops.get(0).getDestinationName());
        assertEquals(3, stops.get(0).getAllocatedDays());
        assertEquals(new BigDecimal("195.00"), stops.get(0).getEstimatedCost());

        // Stop 2
        assertEquals(2, stops.get(1).getDaySequence());
        assertEquals("Knuckles Mountain Range", stops.get(1).getDestinationName());
        assertEquals(2, stops.get(1).getAllocatedDays());
        assertEquals(new BigDecimal("100.00"), stops.get(1).getEstimatedCost());

        // Verify that Module 5 called Module 4 exactly once
        verify(recommendationService, times(1)).getRecommendations(travelerReq);
    }
}
