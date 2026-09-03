package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HistoricalTravelerIndexItem {

    private UUID travelerId;
    private double[] featureVector;
    private List<DestinationRatingRecord> visitedDestinations;
}
