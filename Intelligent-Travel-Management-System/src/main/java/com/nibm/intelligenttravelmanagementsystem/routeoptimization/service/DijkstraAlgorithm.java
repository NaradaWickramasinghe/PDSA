package com.nibm.intelligenttravelmanagementsystem.routeoptimization.service;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.RouteEdge;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.Location;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.TransportMode;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DijkstraAlgorithm {

    public List<Location> findShortestPath(
            Map<Long, List<RouteEdge>> graph,
            Location start,
            Location end,
            TransportMode mode,
            boolean prioritizeTime) {

        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Location> prev = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        PriorityQueue<Long> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        // Initialize distances
        for (Long id : graph.keySet()) {
            dist.put(id, Double.MAX_VALUE);
        }
        dist.put(start.getId(), 0.0);
        pq.add(start.getId());

        while (!pq.isEmpty()) {
            Long currentId = pq.poll();
            if (visited.contains(currentId)) continue;
            visited.add(currentId);

            if (currentId.equals(end.getId())) break;

            List<RouteEdge> edges = graph.getOrDefault(currentId, new ArrayList<>());
            for (RouteEdge edge : edges) {
                Long neighborId = edge.getDestination().getId();
                if (visited.contains(neighborId)) continue;

                // 🔥 FIX: Calculate edge weight based on criteria
                double edgeWeight;
                if (prioritizeTime) {
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