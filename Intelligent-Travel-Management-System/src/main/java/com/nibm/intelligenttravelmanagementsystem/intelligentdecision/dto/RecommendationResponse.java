package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecommendationResponse {

    private UUID travelerId;
    private int totalCandidatesEvaluated;
    private List<DestinationRecommendation> recommendations;
    private String decisionTreePrimaryPrediction;
    private String summaryRationale;
    private OffsetDateTime generatedAt;
}
