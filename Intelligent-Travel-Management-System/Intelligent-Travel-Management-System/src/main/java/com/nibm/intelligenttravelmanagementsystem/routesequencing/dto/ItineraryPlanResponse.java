package com.nibm.intelligenttravelmanagementsystem.routesequencing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItineraryPlanResponse {

    private int totalDays;
    private BigDecimal totalEstimatedCost;
    private int totalDestinationsCount;
    private List<ItineraryDayStop> sequencedStops;
    private String primaryOptimizationSummary;
    private OffsetDateTime generatedAt;
}
