package com.nibm.intelligenttravelmanagementsystem.routeoptimization.service;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.RouteEdge;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.Location;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.TransportMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AStarAlgorithm {

    private final TrafficService trafficService;

    /**
     * Find optimal path using A* algorithm
     * @param prioritizeTime - if true, optimizes for time instead of distance
     */
    public List<Location> findOptimalPath(
            Map<String, List<RouteEdge>> graph,
            Location start,
            Location end,
            TransportMode mode,
            boolean prioritizeTime,
            boolean preferSafeRoute,
            Integer maxRiskLevel){

        // Apply traffic simulation first
        trafficService.applyTrafficSimulation(graph);

        // Priority queue for open set (min-heap by f-score)
        PriorityQueue<NodeInfo> openSet = new PriorityQueue<>();
        // Maps to track scores
        Map<String, Double> gScore = new HashMap<>(); // Cost from start
        Map<String, Double> fScore = new HashMap<>(); // g + heuristic
        Map<String, Location> cameFrom = new HashMap<>();
        Set<String> closedSet = new HashSet<>();

        // Initialize
        gScore.put(start.getId(), 0.0);
        fScore.put(start.getId(), heuristic(start, end, prioritizeTime));
        openSet.add(new NodeInfo(start.getId(), fScore.get(start.getId())));

        while (!openSet.isEmpty()) {
            NodeInfo current = openSet.poll();
            String currentId = current.locationId;

            // Found the goal
            if (currentId.equals(end.getId())) {
                return reconstructPath(cameFrom, start, end);
            }

            // Skip if already evaluated
            if (closedSet.contains(currentId)) continue;
            closedSet.add(currentId);

            // Explore neighbors
            List<RouteEdge> edges = graph.getOrDefault(currentId, new ArrayList<>());

            if (preferSafeRoute || maxRiskLevel != null) {
                int maxRisk = maxRiskLevel != null ? maxRiskLevel : (preferSafeRoute ? 3 : 5);
                edges = edges.stream()
                        .filter(e -> e.getRiskLevel() <= maxRisk)
                        .toList();
            }

            for (RouteEdge edge : edges) {
                String neighborId = edge.getDestination().getId();
                if (closedSet.contains(neighborId)) continue;

                // Calculate edge weight based on criteria
                double edgeWeight;

                if (preferSafeRoute) {
                    double baseWeight = prioritizeTime ?
                            edge.getEffectiveTimeForRoute(mode):
                            edge.getDistanceKm();
                    double riskPenalty = (edge.getRiskLevel() - 1) * 2.0;
                    edgeWeight = baseWeight + riskPenalty;
                }
                else if (prioritizeTime) {
                    // USE TRAFFIC-AWARE EFFECTIVE TIME
                    edgeWeight = edge.getEffectiveTimeForRoute(mode);
                } else {
                    // For shortest path, still use distance but with traffic consideration
                    // Add small penalty for traffic on shortest path too
                    int trafficLevel = trafficService.getTrafficLevel(edge.getId(), edge);
                    double trafficPenalty = 1 + (trafficLevel - 1) * 0.05; // Up to 20% penalty
                    edgeWeight = edge.getDistanceKm() * trafficPenalty;
                }

                double tentativeG = gScore.get(currentId) + edgeWeight;

                if (!gScore.containsKey(neighborId) || tentativeG < gScore.get(neighborId)) {
                    // Better path found
                    cameFrom.put(neighborId, edge.getSource());
                    gScore.put(neighborId, tentativeG);

                    double h = heuristic(edge.getDestination(), end, prioritizeTime);
                    double f = tentativeG + h;
                    fScore.put(neighborId, f);

                    openSet.add(new NodeInfo(neighborId, f));
                }
            }
        }

        // No path found - fallback to Dijkstra
        System.out.println("⚠️ A* found no path, falling back to Dijkstra");
        return new DijkstraAlgorithm().findShortestPath(graph, start, end, mode, prioritizeTime, preferSafeRoute, maxRiskLevel);
    }

    /**
     * Heuristic function - must be admissible (never overestimate)
     * For time: use straight-line time estimate based on distance
     */
    private double heuristic(Location a, Location b, boolean prioritizeTime) {
        // Calculate straight-line distance in km
        double lat1 = a.getLatitude();
        double lon1 = a.getLongitude();
        double lat2 = b.getLatitude();
        double lon2 = b.getLongitude();

        // Rough conversion: 1 degree ≈ 111 km
        double dx = (lat2 - lat1) * 111;
        double dy = (lon2 - lon1) * 111 * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double distanceKm = Math.sqrt(dx * dx + dy * dy);

        if (prioritizeTime) {
            // Assume 60 km/h average with traffic (more conservative)
            return (distanceKm / 60.0) * 60;
        } else {
            return distanceKm;
        }
    }

    // Reconstruct path from cameFrom map
    private List<Location> reconstructPath(Map<String, Location> cameFrom, Location start, Location end) {
        List<Location> path = new ArrayList<>();
        Location current = end;

        while (current != null && !current.equals(start)) {
            path.add(0, current);
            current = cameFrom.get(current.getId());
        }

        if (current != null) {
            path.add(0, start);
        }

        return path;
    }

    // Helper class for priority queue
    private static class NodeInfo implements Comparable<NodeInfo> {
        String locationId;
        double fScore;

        NodeInfo(String id, double f) {
            this.locationId = id;
            this.fScore = f;
        }

        @Override
        public int compareTo(NodeInfo other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }
}