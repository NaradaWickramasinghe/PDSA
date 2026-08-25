package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.SuitabilityLabel;
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
public class DestinationRecommendationDTO {

    private UUID destinationId;
    private String destinationName;
    private String province;
    private int rank;
    private double score;                  // Final composite score (0.0 to 1.0)
    private double matchPercentage;        // (0% to 100%)

    private double treeScore;
    private double knnEvidenceScore;
    private double preferenceScore;
    private double budgetScore;
    private double durationScore;

    private BigDecimal averageDailyCost;
    private Integer minimumDays;
    private Integer maximumDays;
    private Integer difficultyLevel;

    private SuitabilityLabel suitabilityLabel;
    private String reason;
}
