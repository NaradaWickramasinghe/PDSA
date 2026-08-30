package com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripInformationSummary {
    private String destination;
    private double durationHours;
    private int travellerCount;
    private Integer tripDurationDays;
}
