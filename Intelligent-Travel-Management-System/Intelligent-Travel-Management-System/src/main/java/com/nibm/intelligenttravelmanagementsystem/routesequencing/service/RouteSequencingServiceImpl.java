package com.nibm.intelligenttravelmanagementsystem.routesequencing.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.DestinationRecommendation;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationResponse;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.RecommendationService;
import com.nibm.intelligenttravelmanagementsystem.routesequencing.dto.ItineraryDayStop;
import com.nibm.intelligenttravelmanagementsystem.routesequencing.dto.ItineraryPlanRequest;
import com.nibm.intelligenttravelmanagementsystem.routesequencing.dto.ItineraryPlanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteSequencingServiceImpl implements RouteSequencingService {

    private final RecommendationService recommendationService;

    @Override
    public ItineraryPlanResponse createPersonalizedItinerary(ItineraryPlanRequest request) {
        log.info("Module 5 RouteSequencing: Invoking Module 4 RecommendationService for traveler profile...");

        // 1. Consume Module 4 Recommendation Engine
        RecommendationResponse recommendationResponse = recommendationService.getRecommendations(request.getTravelerProfile());

        if (recommendationResponse == null || recommendationResponse.getRecommendations().isEmpty()) {
            log.warn("Module 5 received empty recommendations from Module 4.");
            return buildEmptyItinerary();
        }

        int totalAvailableDays = request.getTravelerProfile().getDurationDays() != null
                ? request.getTravelerProfile().getDurationDays()
                : 5;

        List<DestinationRecommendation> rankedCandidates = recommendationResponse.getRecommendations();
        List<ItineraryDayStop> sequencedStops = new ArrayList<>();

        int cumulativeDays = 0;
        BigDecimal cumulativeCost = BigDecimal.ZERO;
        int maxStops = request.getMaxStops() > 0 ? request.getMaxStops() : 3;

        // 2. Greedy Sequence Scheduling over Top Ranked Destinations
        for (DestinationRecommendation rec : rankedCandidates) {
            if (sequencedStops.size() >= maxStops) break;

            int minDays = rec.getMinimumDays() != null ? rec.getMinimumDays() : 2;
            if (cumulativeDays + minDays > totalAvailableDays) {
                // If adding full minDays exceeds total duration, check if we have at least 1 remaining day
                int remainingDays = totalAvailableDays - cumulativeDays;
                if (remainingDays >= 1 && sequencedStops.isEmpty()) {
                    minDays = remainingDays;
                } else {
                    continue;
                }
            }

            BigDecimal dailyCost = rec.getAverageDailyCost() != null ? rec.getAverageDailyCost() : new BigDecimal("65.00");
            BigDecimal stopCost = dailyCost.multiply(BigDecimal.valueOf(minDays));

            sequencedStops.add(ItineraryDayStop.builder()
                    .daySequence(sequencedStops.size() + 1)
                    .destinationId(rec.getDestinationId())
                    .destinationName(rec.getDestination())
                    .province(rec.getProvince())
                    .allocatedDays(minDays)
                    .estimatedCost(stopCost)
                    .recommendationScore(rec.getScore())
                    .recommendationReason(rec.getReason())
                    .build());

            cumulativeDays += minDays;
            cumulativeCost = cumulativeCost.add(stopCost);
        }

        String summary = String.format(
                "Sequenced %d personalized destination(s) across %d days with an estimated cost of $%s based on Module 4 decision model '%s'.",
                sequencedStops.size(),
                cumulativeDays,
                cumulativeCost.toPlainString(),
                recommendationResponse.getDecisionTreePrimaryPrediction()
        );

        return ItineraryPlanResponse.builder()
                .totalDays(cumulativeDays)
                .totalEstimatedCost(cumulativeCost)
                .totalDestinationsCount(sequencedStops.size())
                .sequencedStops(sequencedStops)
                .primaryOptimizationSummary(summary)
                .generatedAt(OffsetDateTime.now())
                .build();
    }

    private ItineraryPlanResponse buildEmptyItinerary() {
        return ItineraryPlanResponse.builder()
                .totalDays(0)
                .totalEstimatedCost(BigDecimal.ZERO)
                .totalDestinationsCount(0)
                .sequencedStops(Collections.emptyList())
                .primaryOptimizationSummary("No destinations available to sequence itinerary.")
                .generatedAt(OffsetDateTime.now())
                .build();
    }
}
