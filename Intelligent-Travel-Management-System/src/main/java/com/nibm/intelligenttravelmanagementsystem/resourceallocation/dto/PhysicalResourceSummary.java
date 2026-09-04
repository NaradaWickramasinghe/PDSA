package com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalResourceSummary {
    private double carryingCapacity;
    private double equipmentWeightUsed;
    private double remainingCapacity;
}
