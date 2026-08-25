package com.nibm.intelligenttravelmanagementsystem.routesequencing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItineraryDayStop {

    private int daySequence;
    private UUID destinationId;
    private String destinationName;
    private String province;
    private int allocatedDays;
    private BigDecimal estimatedCost;
    private double recommendationScore;
    private String recommendationReason;
}
