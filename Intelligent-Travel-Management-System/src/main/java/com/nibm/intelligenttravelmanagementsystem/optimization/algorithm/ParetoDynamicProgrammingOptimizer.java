package com.nibm.intelligenttravelmanagementsystem.optimization.algorithm;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.ObjectiveWeights;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.RouteSolution;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.TravelNetwork;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParetoDynamicProgrammingOptimizer {
    public RouteSolution optimize(TravelNetwork network, String start, String destination,
            double maxTravelTime, double maxBudget, ObjectiveWeights weights) {
        long startTime = System.nanoTime();
        if (!network.containsNode(start) || !network.containsNode(destination)) {
            return failure(startTime, "Origin or destination not found in the travel network.");
        }

        Map<String, List<FrontierState>> frontier = new HashMap<>();
        frontier.put(start, List.of(new FrontierState(List.of(start), 0.0, 0.0, 0.0)));
        int statesExplored = 0;
        int nodesExplored = 0;

        List<String> queue = new ArrayList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.remove(0);
            nodesExplored++;
            List<FrontierState> states = frontier.getOrDefault(current, List.of());
            for (FrontierState state : states) {
                for (Edge edge : network.getOutgoing(current)) {
                    String next = edge.getTargetName();
                    if (next == null) {
                        continue;
                    }
                    statesExplored++;
                    double nextDistance = state.distance + edge.getDistance();
                    double nextTime = state.travelTime + edge.getTravelTime();
                    double nextCost = state.cost + edge.getCost();
                    if (nextTime > maxTravelTime || nextCost > maxBudget) {
                        continue;
                    }

                    List<String> nextRoute = new ArrayList<>(state.route);
                    nextRoute.add(next);
                    FrontierState candidate = new FrontierState(nextRoute, nextDistance, nextTime, nextCost);
                    frontier.computeIfAbsent(next, ignored -> new ArrayList<>())
                            .add(candidate);
                    if (!frontier.containsKey(next)) {
                        queue.add(next);
                    }

                    frontier.put(next, pruneDominated(frontier.get(next)));
                    if (!queue.contains(next)) {
                        queue.add(next);
                    }
                }
            }
        }

        List<FrontierState> destinationStates = frontier.getOrDefault(destination, List.of());
        if (destinationStates.isEmpty()) {
            return failure(startTime, "No feasible Pareto-optimal route satisfied the constraints.");
        }

        FrontierState best = destinationStates.stream()
                .max((a, b) -> Double.compare(score(a, weights, maxTravelTime, maxBudget),
                        score(b, weights, maxTravelTime, maxBudget)))
                .orElse(destinationStates.get(0));

        return new RouteSolution(best.route, best.distance, best.travelTime, best.cost,
                score(best, weights, maxTravelTime, maxBudget), durationMs(startTime),
                nodesExplored, statesExplored, true, null);
    }

    private List<FrontierState> pruneDominated(List<FrontierState> states) {
        List<FrontierState> kept = new ArrayList<>();
        for (FrontierState candidate : states) {
            boolean dominated = false;
            for (FrontierState other : states) {
                if (other == candidate) {
                    continue;
                }
                if (dominates(other, candidate)) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated) {
                kept.add(candidate);
            }
        }
        return kept;
    }

    private boolean dominates(FrontierState a, FrontierState b) {
        boolean betterOrEqual = a.distance <= b.distance && a.travelTime <= b.travelTime && a.cost <= b.cost;
        boolean strictlyBetter = a.distance < b.distance || a.travelTime < b.travelTime || a.cost < b.cost;
        return betterOrEqual && strictlyBetter;
    }

    private double score(FrontierState state, ObjectiveWeights weights, double maxTravelTime, double maxBudget) {
        double normalizedDistance = state.distance / Math.max(1.0, state.distance + 100.0);
        double normalizedTime = state.travelTime / Math.max(1.0, maxTravelTime);
        double normalizedCost = state.cost / Math.max(1.0, maxBudget);
        double weighted = (weights.distanceWeight() * normalizedDistance)
                + (weights.timeWeight() * normalizedTime)
                + (weights.costWeight() * normalizedCost);
        return 100.0 - (weighted * 100.0);
    }

    private RouteSolution failure(long startTime, String message) {
        return new RouteSolution(List.of(), 0.0, 0.0, 0.0, 0.0, durationMs(startTime), 0, 0, false, message);
    }

    private long durationMs(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    private record FrontierState(List<String> route, double distance, double travelTime, double cost) {
    }
}
