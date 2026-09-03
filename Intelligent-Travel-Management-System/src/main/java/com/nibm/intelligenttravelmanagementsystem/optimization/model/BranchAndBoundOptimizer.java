package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import org.springframework.stereotype.Component;

import java.util.PriorityQueue;

@Component
public class BranchAndBoundOptimizer implements OptimizationAlgorithm {

    @Override
    public String getName() {
        return "BRANCH_AND_BOUND";
    }

    @Override
    public OptimizationResult optimize(TravelGraph graph, OptimizationRequest request) {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        String source = request.getSourceNodeId();
        String destination = request.getDestinationNodeId();

        if (!graph.hasNode(source) || !graph.hasNode(destination)) {
            return OptimizationResult.builder()
                    .algorithmName(getName())
                    .success(false)
                    .message("Source or Destination node not found in graph.")
                    .build();
        }

        PriorityQueue<RouteCandidate> queue = new PriorityQueue<>();
        RouteCandidate initialRoute = new RouteCandidate(source);
        CostEvaluator.computeCompositeScore(initialRoute, request);
        queue.add(initialRoute);

        RouteCandidate bestRoute = null;
        int nodesExplored = 0;

        while (!queue.isEmpty()) {
            RouteCandidate current = queue.poll();
            nodesExplored++;
            String currentId = current.getCurrentNodeId();

            if (destination.equals(currentId)) {
                if (CostEvaluator.satisfiesConstraints(current, request)) {
                    if (bestRoute == null || current.getCompositeScore() < bestRoute.getCompositeScore()) {
                        bestRoute = current;
                        break;
                    }
                }
                continue;
            }

            if (bestRoute != null && current.getCompositeScore() >= bestRoute.getCompositeScore()) {
                continue;
            }

            for (TravelEdge edge : graph.getOutgoingEdges(currentId)) {
                if (request.getPreferredModes() != null && !request.getPreferredModes().isEmpty()) {
                    if (!request.getPreferredModes().contains(edge.getTransportMode())) {
                        continue;
                    }
                }

                if (current.containsNode(edge.getDestination())) {
                    continue;
                }

                if (request.getMaxAllowedRisk() != null && edge.getRiskLevel() > request.getMaxAllowedRisk()) {
                    continue;
                }

                RouteCandidate nextRoute = current.deepCopy();
                nextRoute.addStep(edge);

                if (request.getMaxTimeMinutes() != null && nextRoute.getTotalDurationMinutes() > request.getMaxTimeMinutes()) {
                    continue;
                }
                if (request.getMaxBudgetLkr() != null && nextRoute.getTotalCostLkr() > request.getMaxBudgetLkr()) {
                    continue;
                }

                CostEvaluator.computeCompositeScore(nextRoute, request);

                double remainingDistKm = graph.estimateHaversineDistanceKm(edge.getDestination(), destination);
                double heuristicTimeMins = (remainingDistKm / 80.0) * 60.0;
                double heuristicCost = nextRoute.getCompositeScore() + (request.getTimeWeight() * (heuristicTimeMins / 180.0));

                if (bestRoute == null || heuristicCost < bestRoute.getCompositeScore()) {
                    queue.add(nextRoute);
                }
            }
        }

        long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double executionTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
        double memoryUsedKb = Math.max(0.0, (endMem - startMem) / 1024.0);

        if (bestRoute == null) {
            return OptimizationResult.builder()
                    .algorithmName(getName())
                    .success(false)
                    .message("No feasible path found matching constraints.")
                    .executionTimeMs(executionTimeMs)
                    .memoryUsedKb(memoryUsedKb)
                    .nodesExplored(nodesExplored)
                    .build();
        }

        return OptimizationResult.builder()
                .algorithmName(getName())
                .bestRoute(bestRoute)
                .executionTimeMs(executionTimeMs)
                .memoryUsedKb(memoryUsedKb)
                .nodesExplored(nodesExplored)
                .success(true)
                .message("Optimal path found via Branch and Bound.")
                .build();
    }
}
