package com.nibm.intelligenttravelmanagementsystem.routeoptimization.model;

public enum TransportMode {
    NORMAL_VEHICLE(1.0),
    BUS(1.3),      // 30% slower
    BICYCLE(2.5),  // 2.5x slower
    BIKE(1.1);     // 10% slower (motorcycle)

    private final double timeMultiplier;

    TransportMode(double timeMultiplier) {
        this.timeMultiplier = timeMultiplier;
    }

    public double getTimeMultiplier() {
        return timeMultiplier;
    }
}