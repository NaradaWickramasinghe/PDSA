package com.nibm.intelligenttravelmanagementsystem.routeoptimization.service;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.RouteEdge;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.Location;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.TransportMode;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DijkstraAlgorithm {

    public List<Location> findShortestPath(
            Map<String, List<RouteEdge>> graph,
            Location start,
            Location end,
            TransportMode mode,
            boolean prioritizeTime,
            boolean preferSafeRoute,
            Integer maxRiskLevel) {

        Map<String, Double> dist = new HashMap<>();
        Map<String, Location> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        // Initialize distances
        for (String id : graph.keySet()) {
            dist.put(id, Double.MAX_VALUE);
        }
        dist.put(start.getId(), 0.0);
        pq.add(start.getId());

        while (!pq.isEmpty()) {
            String currentId = pq.poll();
            if (visited.contains(currentId)) continue;
            visited.add(currentId);

            if (currentId.equals(end.getId())) break;

            List<RouteEdge> edges = graph.getOrDefault(currentId, new ArrayList<>());
            if (preferSafeRoute || maxRiskLevel != null) {
                int maxRisk = maxRiskLevel != null ? maxRiskLevel : (preferSafeRoute ? 3 : 5);
                edges = edges.stream()
                        .filter(e -> e.getRiskLevel() <= maxRisk)
                        .toList();
            }
            for (RouteEdge edge : edges) {
                String neighborId = edge.getDestination().getId();
                if (visited.contains(neighborId)) continue;

                double edgeWeight;

                if (preferSafeRoute) {
                    double baseWeight = prioritizeTime ?
                            edge.getEstimatedTimeMinutes() * mode.getTimeMultiplier() :
                            edge.getDistanceKm();
                    double riskPenalty = (edge.getRiskLevel() - 1) * 2.0;
                    edgeWeight = baseWeight + riskPenalty;
                } else if (prioritizeTime) {
                    // Use time as weight (in minutes, adjusted for transport mode)
                    edgeWeight = edge.getEstimatedTimeMinutes() * mode.getTimeMultiplier();
                } else {
                    // Use distance as weight
                    edgeWeight = edge.getDistanceKm();
                }

                double alt = dist.get(currentId) + edgeWeight;
                if (alt < dist.get(neighborId)) {
                    dist.put(neighborId, alt);
                    prev.put(neighborId, edge.getSource());
                    pq.add(neighborId);
                }
            }
        }

        // Reconstruct path
        List<Location> path = new ArrayList<>();
        Location current = end;
        while (current != null && !current.equals(start)) {
            path.add(0, current);
            current = prev.get(current.getId());
        }
        if (current != null) {
            path.add(0, start);
        }

        return path;
    }
}