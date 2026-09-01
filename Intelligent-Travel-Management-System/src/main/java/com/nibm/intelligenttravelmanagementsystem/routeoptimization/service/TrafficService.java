package com.nibm.intelligenttravelmanagementsystem.routeoptimization.service;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.RouteEdge;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.Location;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.TransportMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TrafficService {

    // Simulate real-time traffic data
    private final Map<Long, Integer> realTimeTraffic = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> lastUpdate = new ConcurrentHashMap<>();

    // Peak hour definitions
    private static final LocalTime MORNING_PEAK_START = LocalTime.of(7, 0);
    private static final LocalTime MORNING_PEAK_END = LocalTime.of(9, 30);
    private static final LocalTime EVENING_PEAK_START = LocalTime.of(16, 30);
    private static final LocalTime EVENING_PEAK_END = LocalTime.of(19, 0);

    /**
     * Get current traffic level for a specific edge
     */
    public int getTrafficLevel(Long edgeId, RouteEdge edge) {
        // Check if we have real-time data
        if (realTimeTraffic.containsKey(edgeId)) {
            return realTimeTraffic.get(edgeId);
        }

        // Otherwise, estimate based on time and location
        return estimateTrafficLevel(edge);
    }

    /**
     * Estimate traffic based on time of day and road type
     */
    private int estimateTrafficLevel(RouteEdge edge) {
        LocalTime now = LocalTime.now();
        boolean isPeakHour = isPeakHour(now);
        boolean isWeekend = isWeekend();

        // Base traffic from database
        int baseTraffic = edge.getTrafficLevel() != null ? edge.getTrafficLevel() : 1;

        // Adjust for peak hours
        if (isPeakHour) {
            baseTraffic = Math.min(baseTraffic + 2, 5); // Increase traffic
        }

        // Adjust for weekends (less traffic in city areas)
        if (isWeekend && isCityArea(edge)) {
            baseTraffic = Math.max(baseTraffic - 1, 1);
        }

        // Adjust for highways (usually better during peak)
        if ("highway".equalsIgnoreCase(edge.getTransportMode()) && isPeakHour) {
            baseTraffic = Math.min(baseTraffic + 1, 4);
        }

        return baseTraffic;
    }

    /**
     * Update traffic data for an edge (simulating real-time updates)
     */
    public void updateTraffic(Long edgeId, int trafficLevel) {
        realTimeTraffic.put(edgeId, Math.min(Math.max(trafficLevel, 1), 5));
        lastUpdate.put(edgeId, LocalDateTime.now());
        log.info("Traffic updated for edge {}: Level {}", edgeId, trafficLevel);
    }

    /**
     * Apply traffic simulation to all edges
     */
    public void applyTrafficSimulation(Map<Long, List<RouteEdge>> graph) {
        log.info("Applying traffic simulation...");

        for (List<RouteEdge> edges : graph.values()) {
            for (RouteEdge edge : edges) {
                int trafficLevel = getTrafficLevel(edge.getId(), edge);
                edge.updateWithTraffic(trafficLevel);
            }
        }
    }

    /**
     * Get traffic-aware time for an edge
     */
    public int getEffectiveTime(RouteEdge edge, TransportMode mode) {
         int baseTime = edge.getEstimatedTimeMinutes();
         double timeMultiplier = mode.getTimeMultiplier();

        // Apply traffic multiplier
        int trafficLevel = getTrafficLevel(edge.getId(), edge);
        double trafficMultiplier = getTrafficMultiplier(trafficLevel);

        // Apply road quality multiplier
        double qualityMultiplier = getQualityMultiplier(edge.getRoadQuality());

        return (int) Math.round(baseTime * timeMultiplier * trafficMultiplier * qualityMultiplier);
    }

    private double getTrafficMultiplier(int level) {
        switch (level) {
            case 1: return 1.0;
            case 2: return 1.2;
            case 3: return 1.5;
            case 4: return 2.0;
            case 5: return 3.0;
            default: return 1.0;
        }
    }

    private double getQualityMultiplier(Integer quality) {
        if (quality == null) return 1.0;
        switch (quality) {
            case 1: return 1.0;
            case 2: return 1.1;
            case 3: return 1.3;
            case 4: return 1.6;
            case 5: return 2.0;
            default: return 1.0;
        }
    }
    /**
     * Check if current time is peak hour
     */
    public boolean isPeakHour() {
        return isPeakHour(LocalTime.now());
    }

    public boolean isPeakHour(LocalTime time) {
        return (time.isAfter(MORNING_PEAK_START) && time.isBefore(MORNING_PEAK_END)) ||
                (time.isAfter(EVENING_PEAK_START) && time.isBefore(EVENING_PEAK_END));
    }

    public boolean isWeekend() {
        LocalDateTime now = LocalDateTime.now();
        java.time.DayOfWeek day = now.getDayOfWeek();
        return day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY;
    }

    private boolean isCityArea(RouteEdge edge) {
        // Check if source or destination is in a city
        Location source = edge.getSource();
        Location dest = edge.getDestination();

        String[] cityTypes = {"city", "hub", "urban"};
        for (String type : cityTypes) {
            if (type.equalsIgnoreCase(source.getType()) || type.equalsIgnoreCase(dest.getType())) {
                return true;
            }
        }
        return false;
    }

//     Get traffic summary for a path

    public Map<String, Object> getTrafficSummary(List<RouteEdge> edges) {
        Map<String, Object> summary = new HashMap<>();
        int totalEdges = 0;
        double avgTraffic = 0;
        int totalCongested = 0;

        for (RouteEdge edge : edges) {
            int traffic = getTrafficLevel(edge.getId(), edge);
            avgTraffic += traffic;
            totalEdges++;
            if (traffic >= 4) totalCongested++;


        }

        summary.put("averageTraffic", totalEdges > 0 ? avgTraffic / totalEdges : 0);
        summary.put("congestedSegments", totalCongested);
        summary.put("totalSegments", totalEdges);
        summary.put("status", totalCongested > totalEdges / 2 ? "HEAVY" : "MODERATE");

        return summary;
    }
}

