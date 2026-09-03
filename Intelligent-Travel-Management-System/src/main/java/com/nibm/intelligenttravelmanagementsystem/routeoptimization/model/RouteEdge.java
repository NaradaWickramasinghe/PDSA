package com.nibm.intelligenttravelmanagementsystem.routeoptimization.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteEdge {
    private String id;  // String ID
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


    // Constructor with String ID
    public RouteEdge(String id, Location source, Location destination, double distanceKm,
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
    private double getRoadTypeSpeed() {
        if (transportMode == null) return 50.0;

        switch (transportMode.toUpperCase()) {
            case "HIGHWAY":
                return 90.0;      // 80-100 km/h
            case "EXPRESSWAY":   // Alternative value
                return 90.0;
            case "ROAD":
                return 50.0;      // 40-60 km/h
            case "MAIN_ROAD":    // Alternative value
                return 55.0;
            case "FERRY":
                return 18.0;      // 15-20 km/h
            case "AIR":
                return 300.0;     // Not used
            default:
                return 50.0;
        }
    }

    public int calculateEffectiveTime() {
        double trafficMultiplier = getTrafficMultiplier();
        double qualityMultiplier = getQualityMultiplier();

        double effectiveTime = estimatedTimeMinutes * trafficMultiplier * qualityMultiplier;
        return (int) Math.round(effectiveTime);
    }

    public int getEffectiveTimeForRoute(TransportMode mode) {
        // Start with partner's calculated time (includes traffic and quality)
        double speed = getRoadTypeSpeed();
        double calculatedTime = (distanceKm / speed) * 60;
        if (mode != null) {
            calculatedTime *= mode.getTimeMultiplier();
        }

        // Ensure minimum time of 5 minutes
        return (int) Math.round(Math.max(calculatedTime, 5));
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

    public double getExpectedSpeed(TransportMode userTransport) {
        double baseSpeed = getRoadTypeSpeed();
        if (userTransport != null) {
            baseSpeed /= userTransport.getTimeMultiplier();
        }
        return Math.max(baseSpeed, 10.0);
    }
}