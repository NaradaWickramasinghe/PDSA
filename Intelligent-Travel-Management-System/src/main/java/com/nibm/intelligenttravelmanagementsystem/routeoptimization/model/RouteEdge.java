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

    // Traffic-related fields
    private Integer trafficLevel;      // 1-5
    private Integer roadQuality;       // 1-5
    private Integer accesibility;      // 1-5
    private Double estimatedCostLkr;

    // Time with traffic applied
    private int effectiveTimeMinutes;  // Calculated with traffic


    // Additional fields from shared Edge
//    private Double estimatedCostLkr;
//    private Integer roadQuality;
//    private Integer trafficLevel;
//    private Integer accesibility;

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
        this.effectiveTimeMinutes = estimatedTimeMinutes;
        this.trafficLevel = 1; // Default: low traffic
        this.roadQuality = 1;
    }

    /**
     * Calculate effective time based on traffic level
     */
    public int getEffectiveTimeMinutes() {
        if (effectiveTimeMinutes > 0) return effectiveTimeMinutes;
        return calculateEffectiveTime();
    }

    public int calculateEffectiveTime() {
        double trafficMultiplier = getTrafficMultiplier();
        double qualityMultiplier = getQualityMultiplier();

        double effectiveTime = estimatedTimeMinutes * trafficMultiplier * qualityMultiplier;
        return (int) Math.round(effectiveTime);
    }

    private double getTrafficMultiplier() {
        if (trafficLevel == null) return 1.0;
        switch (trafficLevel) {
            case 1: return 1.0;      // Low traffic
            case 2: return 1.2;      // Mild
            case 3: return 1.5;      // Moderate
            case 4: return 2.0;      // Heavy
            case 5: return 3.0;      // Severe
            default: return 1.0;
        }
    }

    private double getQualityMultiplier() {
        if (roadQuality == null) return 1.0;
        // Road quality: 1=excellent, 5=very poor
        switch (roadQuality) {
            case 1: return 1.0;      // Excellent
            case 2: return 1.1;      // Good
            case 3: return 1.3;      // Average
            case 4: return 1.6;      // Poor
            case 5: return 2.0;      // Very poor
            default: return 1.0;
        }
    }

    public void updateWithTraffic(int trafficLevel) {
        this.trafficLevel = trafficLevel;
        this.effectiveTimeMinutes = calculateEffectiveTime();
    }

    public String getTrafficStatus() {
        if (trafficLevel == null) return "Unknown";
        switch (trafficLevel) {
            case 1: return "🟢 Low Traffic";
            case 2: return "🟡 Mild Traffic";
            case 3: return "🟠 Moderate Traffic";
            case 4: return "🔴 Heavy Traffic";
            case 5: return "⛔ Severe Traffic";
            default: return "Unknown";
        }
    }
}