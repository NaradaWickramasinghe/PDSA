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
public class AllocationProblem {
    private double totalBudget;
    private double emergencyReserve;
    private double maxAvailableHours;
    private double maxCarryingCapacityKg;
    private Integer travellerCount;
    private Integer tripDurationDays;
    private String destination;
    
    @Builder.Default
    private List<ResourceOption> candidateOptions = new ArrayList<>();

    public double getEffectiveBudget() {
        return Math.max(0.0, totalBudget - emergencyReserve);
    }
}
