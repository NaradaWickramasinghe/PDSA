package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NeighborMatch {
    private UUID travelerId;
    private double distance;
    private double similarityWeight;
    private HistoricalTravelerIndexItem travelerItem;
}
