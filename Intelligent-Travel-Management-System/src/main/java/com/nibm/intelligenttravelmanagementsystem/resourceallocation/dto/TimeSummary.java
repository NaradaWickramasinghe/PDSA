package com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSummary {
    private double totalAvailableTime;
    private double transportationTime;
    private double activityTime;
    private double totalTimeUsed;
    private double remainingTime;
}
