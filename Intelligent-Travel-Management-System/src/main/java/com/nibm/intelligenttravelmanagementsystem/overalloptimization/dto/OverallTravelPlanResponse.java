package com.nibm.intelligenttravelmanagementsystem.overalloptimization.dto;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverallTravelPlanResponse {
    private String planName;
    private boolean planOptimized;
    private ResourceAllocationResponse allocatedResources;
    private double totalBudgetUsed;
    private double totalUtilityScore;
    private String optimizationSummary;
}
