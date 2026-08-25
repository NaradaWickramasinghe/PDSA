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
public class DestinationRatingRecord {
    private UUID destinationId;
    private String destinationName;
    private int rating; // 1 to 5
}
