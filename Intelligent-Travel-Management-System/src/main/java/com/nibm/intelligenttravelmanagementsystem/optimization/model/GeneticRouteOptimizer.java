package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GeneticRouteOptimizer implements OptimizationAlgorithm {

    private static final int POPULATION_SIZE = 30;
    private static final int GENERATIONS = 40;
    private static final int TOURNAMENT_SIZE = 3;
    private static final double MUTATION_RATE = 0.25;
    private static final int ELITISM_COUNT = 2;

    private final Random random = new Random(42);

    @Override
    public String getName() {
        return "GENETIC_ALGORITHM";
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

        List<RouteCandidate> population = initializePopulation(graph, request, source, destination, POPULATION_SIZE);

        if (population.isEmpty()) {
            return OptimizationResult.builder()
                    .algorithmName(getName())
                    .success(false)
                    .message("Could not initialize valid paths in Genetic Algorithm.")
                    .build();
        }

        int totalEvaluations = population.size();

        for (int gen = 0; gen < GENERATIONS; gen++) {
            population.sort((a, b) -> Double.compare(calculateFitness(b, request), calculateFitness(a, request)));

            List<RouteCandidate> nextGen = new ArrayList<>();

            for (int e = 0; e < Math.min(ELITISM_COUNT, population.size()); e++) {
                nextGen.add(population.get(e).deepCopy());
            }

            while (nextGen.size() < POPULATION_SIZE) {
                RouteCandidate parent1 = tournamentSelect(population, request);
                RouteCandidate parent2 = tournamentSelect(population, request);

                RouteCandidate offspring = crossover(parent1, parent2, graph, destination);
                if (offspring == null) {
                    offspring = parent1.deepCopy();
                }

                if (random.nextDouble() < MUTATION_RATE) {
                    offspring = mutate(offspring, graph, destination);
                }

                CostEvaluator.computeCompositeScore(offspring, request);
                nextGen.add(offspring);
                totalEvaluations++;
            }

            population = nextGen;
        }

        population.sort((a, b) -> Double.compare(calculateFitness(b, request), calculateFitness(a, request)));
        RouteCandidate bestRoute = population.get(0);

        long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double executionTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
        double memoryUsedKb = Math.max(0.0, (endMem - startMem) / 1024.0);

        boolean valid = CostEvaluator.satisfiesConstraints(bestRoute, request);

        return OptimizationResult.builder()
                .algorithmName(getName())
                .bestRoute(bestRoute)
                .executionTimeMs(executionTimeMs)
                .memoryUsedKb(memoryUsedKb)
                .nodesExplored(totalEvaluations)
                .success(valid)
                .message(valid ? "Near-optimal solution evolved via Genetic Algorithm." : "Suboptimal route evolved with minor constraint overruns.")
                .build();
    }

    private double calculateFitness(RouteCandidate route, OptimizationRequest request) {
        double score = CostEvaluator.computeCompositeScore(route, request);
        double fitness = 100.0 / (1.0 + score);

        if (request.getMaxTimeMinutes() != null && route.getTotalDurationMinutes() > request.getMaxTimeMinutes()) {
            fitness *= 0.5;
        }
        if (request.getMaxBudgetLkr() != null && route.getTotalCostLkr() > request.getMaxBudgetLkr()) {
            fitness *= 0.5;
        }
        if (request.getMaxAllowedRisk() != null && route.getMaxRiskObserved() > request.getMaxAllowedRisk()) {
            fitness *= 0.5;
        }
        return fitness;
    }

    private List<RouteCandidate> initializePopulation(TravelGraph graph, OptimizationRequest request, String source, String destination, int size) {
        List<RouteCandidate> population = new ArrayList<>();
        int attempts = 0;

        while (population.size() < size && attempts < size * 10) {
            attempts++;
            RouteCandidate candidate = randomWalkPath(graph, source, destination, 30);
            if (candidate != null) {
                CostEvaluator.computeCompositeScore(candidate, request);
                population.add(candidate);
            }
        }
        return population;
    }

    private RouteCandidate randomWalkPath(TravelGraph graph, String current, String destination, int maxHops) {
        RouteCandidate route = new RouteCandidate(current);

        for (int step = 0; step < maxHops; step++) {
            String currNode = route.getCurrentNodeId();
            if (destination.equals(currNode)) {
                return route;
            }

            List<TravelEdge> outgoing = graph.getOutgoingEdges(currNode);
            List<TravelEdge> validEdges = new ArrayList<>();
            for (TravelEdge edge : outgoing) {
                if (!route.containsNode(edge.getDestination())) {
                    validEdges.add(edge);
                }
            }

            if (validEdges.isEmpty()) {
                return null;
            }

            TravelEdge chosen = validEdges.get(random.nextInt(validEdges.size()));
            route.addStep(chosen);
        }

        return destination.equals(route.getCurrentNodeId()) ? route : null;
    }

    private RouteCandidate tournamentSelect(List<RouteCandidate> population, OptimizationRequest request) {
        RouteCandidate best = null;
        double bestFitness = -1.0;

        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            RouteCandidate ind = population.get(random.nextInt(population.size()));
            double fit = calculateFitness(ind, request);
            if (fit > bestFitness) {
                bestFitness = fit;
                best = ind;
            }
        }
        return best != null ? best : population.get(0);
    }

    private RouteCandidate crossover(RouteCandidate p1, RouteCandidate p2, TravelGraph graph, String destination) {
        List<String> commonNodes = new ArrayList<>();
        for (int i = 1; i < p1.getNodeIds().size() - 1; i++) {
            String node = p1.getNodeIds().get(i);
            if (p2.getNodeIds().contains(node)) {
                commonNodes.add(node);
            }
        }

        if (commonNodes.isEmpty()) {
            return p1.deepCopy();
        }

        String crossPoint = commonNodes.get(random.nextInt(commonNodes.size()));
        RouteCandidate child = new RouteCandidate(p1.getNodeIds().get(0));

        for (TravelEdge edge : p1.getEdges()) {
            child.addStep(edge);
            if (edge.getDestination().equals(crossPoint)) {
                break;
            }
        }

        boolean copying = false;
        for (TravelEdge edge : p2.getEdges()) {
            if (copying) {
                if (!child.containsNode(edge.getDestination())) {
                    child.addStep(edge);
                } else {
                    return p1.deepCopy();
                }
            } else if (edge.getSource().equals(crossPoint)) {
                copying = true;
                if (!child.containsNode(edge.getDestination())) {
                    child.addStep(edge);
                }
            }
        }

        return destination.equals(child.getCurrentNodeId()) ? child : p1.deepCopy();
    }

    private RouteCandidate mutate(RouteCandidate route, TravelGraph graph, String destination) {
        if (route.getNodeIds().size() <= 2) {
            return route;
        }

        int mutateIdx = 1 + random.nextInt(route.getNodeIds().size() - 2);
        String mutateNode = route.getNodeIds().get(mutateIdx);

        RouteCandidate mutant = new RouteCandidate(route.getNodeIds().get(0));
        for (int i = 0; i < mutateIdx; i++) {
            mutant.addStep(route.getEdges().get(i));
        }

        RouteCandidate suffix = randomWalkPath(graph, mutateNode, destination, 20);
        if (suffix != null) {
            for (TravelEdge edge : suffix.getEdges()) {
                if (!mutant.containsNode(edge.getDestination())) {
                    mutant.addStep(edge);
                } else {
                    return route;
                }
            }
            if (destination.equals(mutant.getCurrentNodeId())) {
                return mutant;
            }
        }

        return route;
    }
}
