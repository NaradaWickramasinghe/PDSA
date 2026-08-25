package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model;

public enum SuitabilityLabel {
    EXCELLENT_FIT(1.0),
    MODERATE_FIT(0.7),
    CHALLENGING_FIT(0.4),
    NOT_SUITABLE(0.0);

    private final double baseScore;

    SuitabilityLabel(double baseScore) {
        this.baseScore = baseScore;
    }

    public double getBaseScore() {
        return baseScore;
    }
}
