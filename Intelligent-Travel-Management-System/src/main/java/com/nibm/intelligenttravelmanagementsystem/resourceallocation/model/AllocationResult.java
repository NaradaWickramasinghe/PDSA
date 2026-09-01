package com.nibm.intelligenttravelmanagementsystem.resourceallocation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationResult {
    private String algorithmName;
    private boolean feasible;
    
    @Builder.Default
    private List<ResourceOption> selectedResources = new ArrayList<>();
    
    private double totalCost;
    private double remainingBudget;
    private double totalTime;
    private double remainingTime;
    private double totalWeight;
    private double remainingCapacity;
    private double overallScore;
    private long executionTimeMs;
    private String statusMessage;

    public static AllocationResult infeasible(String algorithmName, String message) {
        return AllocationResult.builder()
                .algorithmName(algorithmName)
                .feasible(false)
                .selectedResources(new ArrayList<>())
                .totalCost(0.0)
                .remainingBudget(0.0)
                .totalTime(0.0)
                .remainingTime(0.0)
                .totalWeight(0.0)
                .remainingCapacity(0.0)
                .overallScore(0.0)
                .executionTimeMs(0)
                .statusMessage(message)
                .build();
    }
}
