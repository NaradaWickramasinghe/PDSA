package com.nibm.intelligenttravelmanagementsystem.overalloptimization.dto;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverallTravelPlanRequest {
    private String planName;
    private Double maxOverallTravelDays;
    private ResourceAllocationRequest resourceAllocationRequest;
}
