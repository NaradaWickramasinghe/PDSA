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

    public RouteResult findRoute(String startId, String endId, TransportMode mode, boolean prioritizeTime, boolean preferSafeRoute,
                                 Integer maxRiskLevel) {
        Location start = graphService.getLocation(startId);
        Location end = graphService.getLocation(endId);

        if (start == null || end == null) {
            throw new IllegalArgumentException("Invalid start or end location");
        }

        // Use full graph (no risk filtering)
        Map<String, List<RouteEdge>> graph = graphService.getGraph();

        long startTime = System.nanoTime();
        List<Location> path = aStarAlgorithm.findOptimalPath(
                graph, start, end, mode, prioritizeTime, preferSafeRoute, maxRiskLevel
        );
        long endTime = System.nanoTime();

        if (path.isEmpty()) {
            throw new RuntimeException("No path found between " + start.getName() + " and " + end.getName());
        }

        RouteResult result = calculateMetrics(path, graph, mode);
        result.setExecutionTimeMs((endTime - startTime) / 1_000_000.0);
        result.setAlgorithmUsed("A*");

        return result;
    }

    public RouteResult findMultiStopRoute(String startLocationId, List<String> destinationIds, TransportMode mode, boolean prioritizeTime, boolean preferSafeRoute, Integer maxRiskLevel) {
        if (destinationIds == null || destinationIds.size() < 1) {
            throw new IllegalArgumentException("Need at least 1 destination");
        }

        // Get start location
        Location start = graphService.getLocation(startLocationId);
        if (start == null) {
            throw new IllegalArgumentException("Invalid start location");
        }

        // Get all destinations
        List<Location> destinations = destinationIds.stream()
                .map(graphService::getLocation)
                .filter(Objects::nonNull)
                .toList();

        if (destinations.size() != destinationIds.size()) {
            throw new IllegalArgumentException("Some destinations not found");
        }

        Map<String, List<RouteEdge>> graph = graphService.getGraph();

        // Create a list with start + destinations
        List<Location> allLocations = new ArrayList<>();
        allLocations.add(start);  // Start is at index 0
        allLocations.addAll(destinations);  // Destinations are at indices 1..n

        int n = allLocations.size();
        double[][] weightMatrix = new double[n][n];

        // Build weight matrix between all locations (including start)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    weightMatrix[i][j] = 0;
                } else {
                    List<Location> path = aStarAlgorithm.findOptimalPath(
                            graph, allLocations.get(i), allLocations.get(j), mode, prioritizeTime, preferSafeRoute, maxRiskLevel
                    );

                    if (!path.isEmpty()) {
                        if (prioritizeTime) {
                            RouteResult temp = calculateMetrics(path, graph, mode);
                            weightMatrix[i][j] = temp.getEstimatedTimeMinutes();
                        } else {
                            RouteResult temp = calculateMetrics(path, graph, mode);
                            weightMatrix[i][j] = temp.getTotalDistanceKm();
                        }
                    } else {
                        weightMatrix[i][j] = Double.MAX_VALUE;
                    }
                }
            }
        }

        // Solve TSP with fixed start (index 0)
        List<Integer> optimalOrder = solveTSPWithFixedStart(weightMatrix);

        // Build full path
        List<Location> fullPath = new ArrayList<>();
        double totalDistance = 0;
        double totalTime = 0;
        double totalRisk = 0;
        int segmentCount = 0;

        // Build path from the ordered locations
        List<Location> orderedLocations = new ArrayList<>();
        for (int idx : optimalOrder) {
            orderedLocations.add(allLocations.get(idx));
        }

        // Build the actual route path
        for (int i = 0; i < orderedLocations.size() - 1; i++) {
            Location from = orderedLocations.get(i);
            Location to = orderedLocations.get(i + 1);

            List<Location> segmentPath = aStarAlgorithm.findOptimalPath(
                    graph, from, to, mode, prioritizeTime, preferSafeRoute, maxRiskLevel
            );

            if (segmentPath.size() > 1) {
                if (fullPath.isEmpty()) {
                    fullPath.addAll(segmentPath);
                } else {
                    fullPath.addAll(segmentPath.subList(1, segmentPath.size()));
                }

                RouteResult segment = calculateMetrics(segmentPath, graph, mode);
                totalDistance += segment.getTotalDistanceKm();
                totalTime += segment.getEstimatedTimeMinutes();
                totalRisk += segment.getRiskScore();
                segmentCount++;
            }
        }

        return RouteResult.builder()
                .path(fullPath)
                .orderedLocations(orderedLocations)
                .totalDistanceKm(totalDistance)
                .estimatedTimeMinutes(totalTime)
                .riskScore(segmentCount > 0 ? totalRisk / segmentCount : 0)
                .algorithmUsed("A* + TSP (Fixed Start)" + (prioritizeTime ? " - Fastest" : " - Shortest"))
                .build();
    }

    /**
     * Solve TSP with fixed start point (index 0 must be first)
     */
    private List<Integer> solveTSPWithFixedStart(double[][] weightMatrix) {
        int n = weightMatrix.length;

        // If only start + 1 destination, just return [0, 1]
        if (n == 2) {
            return Arrays.asList(0, 1);
        }

        // Start from index 0
        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[n];

        // Always start with index 0 (the start location)
        order.add(0);
        visited[0] = true;

        // Nearest neighbor for remaining destinations
        while (order.size() < n) {
            int last = order.get(order.size() - 1);
            int nearest = -1;
            double minDist = Double.MAX_VALUE;

            for (int j = 0; j < n; j++) {
                if (!visited[j] && weightMatrix[last][j] < minDist) {
                    minDist = weightMatrix[last][j];
                    nearest = j;
                }
            }

            if (nearest != -1) {
                order.add(nearest);
                visited[nearest] = true;
            } else {
                break;
            }
        }

        // Apply 2-opt optimization (but keep index 0 fixed)
        order = twoOptOptimizationWithFixedStart(order, weightMatrix);

        return order;
    }

    /**
     * 2-opt optimization with fixed start point
     */
    private List<Integer> twoOptOptimizationWithFixedStart(List<Integer> order, double[][] weightMatrix) {
        boolean improved = true;
        List<Integer> best = new ArrayList<>(order);
        int n = best.size();

        // We cannot swap index 0 (start must remain first)
        while (improved) {
            improved = false;

            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    // Don't swap if it would move the start point
                    if (i == 0 || j == 0) continue;

                    double currentDist = weightMatrix[best.get(i)][best.get((i + 1) % n)] +
                            weightMatrix[best.get(j)][best.get((j + 1) % n)];

                    double newDist = weightMatrix[best.get(i)][best.get(j)] +
                            weightMatrix[best.get((i + 1) % n)][best.get((j + 1) % n)];

                    if (newDist < currentDist) {
                        List<Integer> newOrder = new ArrayList<>(best);
                        int left = i + 1;
                        int right = j;
                        while (left < right) {
                            Collections.swap(newOrder, left, right);
                            left++;
                            right--;
                        }
                        best = newOrder;
                        improved = true;
                        break;
                    }
                }
                if (improved) break;
            }
        }

        return best;
    }

    private RouteResult calculateMetrics(List<Location> path, Map<String, List<RouteEdge>> graph, TransportMode mode) {
        double totalDistance = 0;
        double totalTime = 0;
        double riskSum = 0;
        int edgeCount = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            Location from = path.get(i);
            Location to = path.get(i + 1);

            List<RouteEdge> edges = graph.get(from.getId());
            for (RouteEdge edge : edges) {
                if (edge.getDestination().getId().equals(to.getId())) {
                    totalDistance += edge.getDistanceKm();
                    totalTime += edge.getEstimatedTimeMinutes() * mode.getTimeMultiplier();
                    riskSum += edge.getRiskLevel();
                    edgeCount++;
                    break;
                }
            }
        }

        return RouteResult.builder()
                .path(path)
                .totalDistanceKm(totalDistance)
                .estimatedTimeMinutes(totalTime)
                .riskScore(edgeCount > 0 ? riskSum / edgeCount : 0)
                .build();
    }
}