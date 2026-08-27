package com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.Location;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RouteResult {
    private List<Location> path;
    private List<Location> orderedLocations; // For multi-stop - shows the optimal order
    private double totalDistanceKm;
    private double estimatedTimeMinutes;
    private double riskScore;
    private String algorithmUsed;
    private double executionTimeMs;
}