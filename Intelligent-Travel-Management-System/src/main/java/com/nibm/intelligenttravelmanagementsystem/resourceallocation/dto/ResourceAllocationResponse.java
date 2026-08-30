package com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto;

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
public class ResourceAllocationResponse {
    private String algorithmUsed;
    private boolean feasible;

    // Legacy / top-level flat fields maintained for backwards frontend compatibility
    @Builder.Default
    private List<SelectedResourceResponse> selectedResources = new ArrayList<>();

    private double totalCost;
    private double remainingBudget;
    private double totalTimeUsed;
    private double remainingTime;
    private double totalWeight;
    private double remainingCapacity;
    private double overallScore;
    private long executionTimeMs;
    private String statusMessage;

    // Structured Travel Resource Plan sections
    private TripInformationSummary tripInformation;
    private FinancialSummary financialSummary;
    private TimeSummary timeSummary;
    private PhysicalResourceSummary physicalResourceSummary;

    @Builder.Default
    private List<SelectedResourceResponse> selectedTransportation = new ArrayList<>();

    @Builder.Default
    private List<SelectedResourceResponse> selectedAccommodation = new ArrayList<>();

    @Builder.Default
    private List<SelectedResourceResponse> selectedActivities = new ArrayList<>();

    @Builder.Default
    private List<SelectedResourceResponse> selectedEquipment = new ArrayList<>();
}
