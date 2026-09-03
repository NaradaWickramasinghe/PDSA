package com.nibm.intelligenttravelmanagementsystem.optimization.dto;

public record ObjectiveWeights(
        double distanceWeight,
        double timeWeight,
        double costWeight) {
    public ObjectiveWeights {
        if (Double.isNaN(distanceWeight) || Double.isNaN(timeWeight) || Double.isNaN(costWeight)) {
            throw new IllegalArgumentException("Objective weights must be numeric values.");
        }
        double total = distanceWeight + timeWeight + costWeight;
        if (total <= 0.0) {
            throw new IllegalArgumentException("Objective weights must sum to a positive value.");
        }
        double scale = 1.0 / total;
        distanceWeight = distanceWeight * scale;
        timeWeight = timeWeight * scale;
        costWeight = costWeight * scale;
    }

    public static ObjectiveWeights defaults() {
        return new ObjectiveWeights(0.45, 0.35, 0.20);
    }
}
