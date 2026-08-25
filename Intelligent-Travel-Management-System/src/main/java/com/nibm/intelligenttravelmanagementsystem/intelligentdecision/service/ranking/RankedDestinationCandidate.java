package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.Destination;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.SuitabilityLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RankedDestinationCandidate {

    private UUID destinationId;
    private String destinationName;
    private int rank;
    private double finalScore;          // Bounded 0.0 to 1.0

    private double treeScore;           // Decision tree contribution
    private double knnScore;            // Collaborative k-NN evidence contribution
    private double preferenceScore;     // Cosine preference similarity
    private double budgetScore;         // Budget compatibility
    private double durationScore;       // Duration compatibility

    private SuitabilityLabel suitabilityLabel;
    private String explanation;
    private Destination destination;
}
