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

    // New traffic fields
    private double trafficScore;      // Average traffic level (1-5)
    private int congestedSegments;    // Number of segments with heavy traffic
    private String trafficStatus;     // "LOW", "MODERATE", "HEAVY"

    // Helper method
    public String getTrafficStatus() {
        if (congestedSegments == 0) return "🟢 Low Traffic";
        if (congestedSegments <= 2) return "🟡 Moderate Traffic";
        if (congestedSegments <= 4) return "🟠 Heavy Traffic";
        return "🔴 Severe Traffic";
    }
}
