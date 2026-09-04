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
public class DestinationRecommendation {

    private String destination;            // Primary destination name (e.g. "Ella")
    private double score;                  // Recommendation score (e.g. 0.86)
    private int rank;                      // 1-based rank (e.g. 1)
    private String reason;                 // Explainability reason

    private UUID destinationId;
    private String province;
    private double matchPercentage;        // (e.g. 86.0%)

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
}
