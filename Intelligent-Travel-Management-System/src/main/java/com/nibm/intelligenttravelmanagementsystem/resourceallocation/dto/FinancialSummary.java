package com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummary {
    private double totalBudget;
    private double emergencyReserve;
    private double availableAllocationBudget;
    private double totalAllocatedCost;
    private double remainingBudget;
}
