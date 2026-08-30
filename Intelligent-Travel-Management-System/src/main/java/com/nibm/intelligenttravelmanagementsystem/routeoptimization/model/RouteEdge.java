package com.nibm.intelligenttravelmanagementsystem.routeoptimization.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteEdge {
    private Long id;
    private Location source;
    private Location destination;
    private double distanceKm;
    private int estimatedTimeMinutes;
    private int riskLevel;
    private String transportMode;
    private boolean isOneWay;

    // Additional fields from shared Edge
    private Double estimatedCostLkr;
    private Integer roadQuality;
    private Integer trafficLevel;
    private Integer accesibility;

    public RouteEdge(Long id, Location source, Location destination, double distanceKm,
                     int estimatedTimeMinutes, int riskLevel, String transportMode, boolean isOneWay) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.riskLevel = riskLevel;
        this.transportMode = transportMode;
        this.isOneWay = isOneWay;
    }
}