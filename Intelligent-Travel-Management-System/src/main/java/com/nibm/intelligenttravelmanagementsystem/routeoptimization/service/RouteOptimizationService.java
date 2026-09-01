package com.nibm.intelligenttravelmanagementsystem.routeoptimization.service;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto.RouteResult;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.RouteEdge;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.Location;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.TransportMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    private final GraphService graphService;
    private final AStarAlgorithm aStarAlgorithm;
    private final TrafficService trafficService;

    // ... existing methods ...

    private RouteResult calculateMetrics(List<Location> path, Map<Long, List<RouteEdge>> graph, TransportMode mode) {
        double totalDistance = 0;
        double totalTime = 0;
        double totalRisk = 0;
        double totalTraffic = 0;
        int edgeCount = 0;
        int congestedCount = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            Location from = path.get(i);
            Location to = path.get(i + 1);

            List<RouteEdge> edges = graph.get(from.getId());
            for (RouteEdge edge : edges) {
                if (edge.getDestination().getId().equals(to.getId())) {
                    totalDistance += edge.getDistanceKm();

                    // Use traffic-aware time
                    int effectiveTime = trafficService.getEffectiveTime(edge, mode);
                    totalTime += effectiveTime;

                    totalRisk += edge.getRiskLevel();

                    // Track traffic
                    int traffic = trafficService.getTrafficLevel(edge.getId(), edge);
                    totalTraffic += traffic;
                    if (traffic >= 4) congestedCount++;

                    edgeCount++;
                    break;
                }
            }
        }

        return RouteResult.builder()
                .path(path)
                .totalDistanceKm(totalDistance)
                .estimatedTimeMinutes(totalTime)
                .riskScore(edgeCount > 0 ? totalRisk / edgeCount : 0)
                .trafficScore(edgeCount > 0 ? totalTraffic / edgeCount : 0)
                .congestedSegments(congestedCount)
                .build();
    }
}

