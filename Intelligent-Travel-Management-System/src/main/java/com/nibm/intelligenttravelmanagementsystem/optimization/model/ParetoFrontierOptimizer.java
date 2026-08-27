package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ParetoFrontierOptimizer implements OptimizationAlgorithm {

    @Override
    public String getName() {
        return "PARETO_DYNAMIC_PROGRAMMING";
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

        Map<String, List<RouteCandidate>> paretoLabels = new HashMap<>();
        for (String nodeId : graph.getNodes().keySet()) {
            paretoLabels.put(nodeId, new ArrayList<>());
        }

        PriorityQueue<RouteCandidate> queue = new PriorityQueue<>();
        RouteCandidate startRoute = new RouteCandidate(source);
        CostEvaluator.computeCompositeScore(startRoute, request);
        paretoLabels.get(source).add(startRoute);
        queue.add(startRoute);

        List<RouteCandidate> destinationParetoSet = new ArrayList<>();
        int nodesExplored = 0;

        while (!queue.isEmpty()) {
            RouteCandidate current = queue.poll();
            nodesExplored++;
            String currentId = current.getCurrentNodeId();

            if (destination.equals(currentId)) {
                if (CostEvaluator.satisfiesConstraints(current, request)) {
                    updateParetoSet(destinationParetoSet, current);
                }
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

                List<RouteCandidate> nodeLabels = paretoLabels.get(edge.getDestination());
                if (nodeLabels != null && updateParetoSet(nodeLabels, nextRoute)) {
                    queue.add(nextRoute);
                }
            }
        }

        long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double executionTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
        double memoryUsedKb = Math.max(0.0, (endMem - startMem) / 1024.0);

        if (destinationParetoSet.isEmpty()) {
            return OptimizationResult.builder()
                    .algorithmName(getName())
                    .success(false)
                    .message("No Pareto-optimal paths found matching constraints.")
                    .executionTimeMs(executionTimeMs)
                    .memoryUsedKb(memoryUsedKb)
                    .nodesExplored(nodesExplored)
                    .build();
        }

        destinationParetoSet.sort(Comparator.comparingDouble(RouteCandidate::getCompositeScore));
        RouteCandidate bestRoute = destinationParetoSet.get(0);

        List<RouteCandidate> alternatives = new ArrayList<>();
        for (int i = 1; i < destinationParetoSet.size(); i++) {
            alternatives.add(destinationParetoSet.get(i));
        }

        return OptimizationResult.builder()
                .algorithmName(getName())
                .bestRoute(bestRoute)
                .paretoAlternatives(alternatives)
                .executionTimeMs(executionTimeMs)
                .memoryUsedKb(memoryUsedKb)
                .nodesExplored(nodesExplored)
                .success(true)
                .message("Pareto Frontier computed with " + destinationParetoSet.size() + " non-dominated solutions.")
                .build();
    }

    private boolean updateParetoSet(List<RouteCandidate> paretoSet, RouteCandidate candidate) {
        for (RouteCandidate existing : paretoSet) {
            if (existing.dominates(candidate)) {
                return false;
            }
        }
        paretoSet.removeIf(candidate::dominates);
        paretoSet.add(candidate);
        return true;
    }
}
