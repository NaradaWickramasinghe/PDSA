package com.nibm.intelligenttravelmanagementsystem.optimization.algorithm;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.ObjectiveWeights;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.RouteSolution;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.TravelNetwork;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BranchAndBoundOptimizer {
    private static final String METHOD_NAME = "BRANCH_AND_BOUND";

    public RouteSolution optimize(TravelNetwork network, String start, String destination,
            double maxTravelTime, double maxBudget, ObjectiveWeights weights) {
        long startTime = System.nanoTime();

        if (!network.containsNode(start) || !network.containsNode(destination)) {
            return failure(startTime, "Origin or destination not found in the travel network.");
        }

        if (start.equals(destination)) {
            return new RouteSolution(List.of(start), 0.0, 0.0, 0.0, 100.0, durationMs(startTime), 1, 1, true, null);
        }

        Map<String, Double> minDistanceToDestination = shortestDistanceMap(network, destination);
        RouteSolution[] bestHolder = new RouteSolution[1];
        List<String> route = new ArrayList<>();
        route.add(start);
        Set<String> visited = new HashSet<>();
        visited.add(start);
        int[] nodesExplored = { 0 };
        int[] statesExplored = { 0 };

        dfs(network, start, destination, maxTravelTime, maxBudget, weights, route, visited,
                0.0, 0.0, 0.0, minDistanceToDestination, bestHolder, nodesExplored, statesExplored);

        if (bestHolder[0] == null) {
            return failure(startTime, "No feasible route satisfies the time and budget constraints.");
        }

        return new RouteSolution(bestHolder[0].getRoute(), bestHolder[0].getTotalDistance(),
                bestHolder[0].getTotalTravelTime(),
                bestHolder[0].getTotalCost(), bestHolder[0].getObjectiveScore(), durationMs(startTime),
                nodesExplored[0], statesExplored[0], true, null);
    }

    private void dfs(TravelNetwork network, String current, String destination, double maxTravelTime,
            double maxBudget, ObjectiveWeights weights, List<String> route, Set<String> visited,
            double distance, double travelTime, double totalCost,
            Map<String, Double> minDistanceToDestination, RouteSolution[] bestHolder,
            int[] nodesExplored, int[] statesExplored) {
        nodesExplored[0]++;
        if (current.equals(destination)) {
            double score = computeObjectiveScore(distance, travelTime, totalCost, weights, maxTravelTime, maxBudget);
            RouteSolution candidate = new RouteSolution(route, distance, travelTime, totalCost, score,
                    0, nodesExplored[0], statesExplored[0], true, null);
            if (bestHolder[0] == null || candidate.getObjectiveScore() > bestHolder[0].getObjectiveScore()) {
                bestHolder[0] = candidate;
            }
            return;
        }

        for (Edge edge : network.getOutgoing(current)) {
            String next = edge.getTargetName();
            if (next == null || visited.contains(next)) {
                continue;
            }
            statesExplored[0]++;
            double nextDistance = distance + edge.getDistance();
            double nextTime = travelTime + edge.getTravelTime();
            double nextCost = totalCost + edge.getCost();

            if (nextTime > maxTravelTime || nextCost > maxBudget) {
                continue;
            }

            double lowerBoundDistance = minDistanceToDestination.getOrDefault(next, 0.0);
            if (bestHolder[0] != null && nextDistance + lowerBoundDistance > bestHolder[0].getTotalDistance()) {
                continue;
            }

            route.add(next);
            visited.add(next);
            dfs(network, next, destination, maxTravelTime, maxBudget, weights, route, visited,
                    nextDistance, nextTime, nextCost, minDistanceToDestination, bestHolder, nodesExplored,
                    statesExplored);
            visited.remove(next);
            route.remove(route.size() - 1);
        }
    }

    private double computeObjectiveScore(double distance, double travelTime, double totalCost,
            ObjectiveWeights weights, double maxTravelTime, double maxBudget) {
        double normalizedDistance = distance <= 0 ? 0.0 : distance / Math.max(1.0, distance + 1.0);
        double normalizedTime = travelTime <= 0 ? 0.0 : travelTime / Math.max(1.0, maxTravelTime);
        double normalizedCost = totalCost <= 0 ? 0.0 : totalCost / Math.max(1.0, maxBudget);
        double weighted = (weights.distanceWeight() * normalizedDistance)
                + (weights.timeWeight() * normalizedTime)
                + (weights.costWeight() * normalizedCost);
        return 100.0 - (weighted * 100.0);
    }

    private Map<String, Double> shortestDistanceMap(TravelNetwork network, String destination) {
        Map<String, Double> results = new java.util.HashMap<>();
        for (String node : network.getNodes().keySet()) {
            results.put(node, 0.0);
        }
        Set<String> settled = new HashSet<>();
        java.util.PriorityQueue<NodeDistance> queue = new java.util.PriorityQueue<>(
                (a, b) -> Double.compare(a.distance, b.distance));
        queue.add(new NodeDistance(destination, 0.0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            if (settled.contains(current.node)) {
                continue;
            }
            settled.add(current.node);
            results.put(current.node, current.distance);
            for (Edge edge : network.getOutgoing(current.node)) {
                String next = edge.getTargetName();
                if (next == null || settled.contains(next)) {
                    continue;
                }
                double candidate = current.distance + edge.getDistance();
                queue.add(new NodeDistance(next, candidate));
            }
        }
        return results;
    }

    private RouteSolution failure(long startTime, String message) {
        return new RouteSolution(List.of(), 0.0, 0.0, 0.0, 0.0, durationMs(startTime), 0, 0, false, message);
    }

    private long durationMs(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    private static class NodeDistance {
        private final String node;
        private final double distance;

        private NodeDistance(String node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }
}
