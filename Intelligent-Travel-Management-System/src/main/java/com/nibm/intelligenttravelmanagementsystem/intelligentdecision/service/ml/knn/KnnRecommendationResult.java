package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KnnRecommendationResult {

    private int kUsed;
    private int totalNeighborsSearched;
    private Map<String, Double> destinationScores;
    private List<KnnDestinationEvidence> topEvidences;
    private List<NeighborMatch> nearestNeighbors;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class KnnDestinationEvidence {
        private String destinationName;
        private double evidenceScore;      // 0.0 to 1.0
        private int voteCount;
        private double averageRating;      // 1.0 to 5.0
        private double totalWeightedScore;
        private String explanation;
    }
}
