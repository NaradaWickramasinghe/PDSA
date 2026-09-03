package com.nibm.intelligenttravelmanagementsystem.optimization.algorithm;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.ObjectiveWeights;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.RouteSolution;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.TravelNetwork;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GeneticAlgorithmOptimizer {
    private static final int POPULATION_SIZE = 30;
    private static final int MAX_GENERATIONS = 60;
    private static final double MUTATION_RATE = 0.25;
    private final Random random = new Random();

    public RouteSolution optimize(TravelNetwork network, String start, String destination,
            double maxTravelTime, double maxBudget, ObjectiveWeights weights) {
        long startTime = System.nanoTime();

        if (!network.containsNode(start) || !network.containsNode(destination)) {
            return failure(startTime, "Origin or destination not found in the travel network.");
        }

        List<List<String>> population = initializePopulation(network, start, destination);
        RouteSolution best = null;
        int generations = 0;
        int nodesExplored = 0;
        int statesExplored = 0;

        while (generations < MAX_GENERATIONS) {
            List<RouteCandidate> evaluated = new ArrayList<>();
            for (List<String> chromosome : population) {
                nodesExplored++;
                RouteCandidate candidate = evaluateChromosome(network, chromosome, maxTravelTime, maxBudget, weights);
                if (candidate != null) {
                    evaluated.add(candidate);
                    statesExplored++;
                }
            }

            if (evaluated.isEmpty()) {
                break;
            }
            evaluated.sort(Comparator.comparingDouble(RouteCandidate::score).reversed());
            if (best == null || evaluated.get(0).score() > best.getObjectiveScore()) {
                best = evaluated.get(0).toSolution(durationMs(startTime), nodesExplored, statesExplored);
            }

            List<List<String>> nextPopulation = new ArrayList<>();
            while (nextPopulation.size() < POPULATION_SIZE) {
                List<String> parentA = tournamentSelect(evaluated);
                List<String> parentB = tournamentSelect(evaluated);
                List<String> child = crossover(parentA, parentB);
                if (random.nextDouble() < MUTATION_RATE) {
                    child = mutate(child, network, start, destination);
                }
                if (!child.isEmpty()) {
                    nextPopulation.add(child);
                }
            }
            population = nextPopulation;
            generations++;
        }

        if (best == null || !best.isSuccess()) {
            return failure(startTime, "No feasible route was found by the genetic algorithm within the constraints.");
        }

        return best;
    }

    private List<List<String>> initializePopulation(TravelNetwork network, String start, String destination) {
        List<List<String>> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<String> route = randomRoute(network, start, destination);
            if (!route.isEmpty()) {
                population.add(route);
            }
        }
        if (population.isEmpty()) {
            population.add(List.of(start, destination));
        }
        return population;
    }

    private List<String> randomRoute(TravelNetwork network, String start, String destination) {
        List<String> route = new ArrayList<>();
        route.add(start);
        Set<String> visited = new HashSet<>();
        visited.add(start);
        String current = start;
        while (!current.equals(destination) && route.size() < 10) {
            List<Edge> choices = network.getOutgoing(current);
            if (choices.isEmpty()) {
                return List.of();
            }
            Collections.shuffle(choices);
            boolean moved = false;
            for (Edge edge : choices) {
                String next = edge.getTargetName();
                if (next == null || visited.contains(next)) {
                    continue;
                }
                route.add(next);
                visited.add(next);
                current = next;
                moved = true;
                break;
            }
            if (!moved) {
                return List.of();
            }
        }
        return route;
    }

    private List<String> tournamentSelect(List<RouteCandidate> population) {
        int first = random.nextInt(population.size());
        int second = random.nextInt(population.size());
        return population.get(Math.max(first, second)).route();
    }

    private List<String> crossover(List<String> parentA, List<String> parentB) {
        if (parentA.isEmpty() || parentB.isEmpty() || parentA.size() < 2 || parentB.size() < 2) {
            return new ArrayList<>();
        }
        int point = random.nextInt(Math.min(parentA.size(), parentB.size()) - 1) + 1;
        List<String> child = new ArrayList<>();
        for (int i = 0; i < point; i++) {
            child.add(parentA.get(i));
        }
        for (String node : parentB) {
            if (!child.contains(node)) {
                child.add(node);
            }
        }
        return child;
    }

    private List<String> mutate(List<String> route, TravelNetwork network, String start, String destination) {
        if (route.size() < 2) {
            return route;
        }
        int indexA = random.nextInt(route.size());
        int indexB = random.nextInt(route.size());
        Collections.swap(route, indexA, indexB);
        if (!route.get(0).equals(start)) {
            route.removeIf(node -> node.equals(start));
            route.add(0, start);
        }
        if (!route.get(route.size() - 1).equals(destination)) {
            route.removeIf(node -> node.equals(destination));
            route.add(destination);
        }
        return route;
    }

    private RouteCandidate evaluateChromosome(TravelNetwork network, List<String> chromosome, double maxTravelTime,
            double maxBudget, ObjectiveWeights weights) {
        if (chromosome.size() < 2) {
            return null;
        }
        double distance = 0.0;
        double travelTime = 0.0;
        double cost = 0.0;
        for (int i = 0; i < chromosome.size() - 1; i++) {
            String source = chromosome.get(i);
            String target = chromosome.get(i + 1);
            Edge edge = findEdge(network, source, target);
            if (edge == null) {
                return null;
            }
            distance += edge.getDistance();
            travelTime += edge.getTravelTime();
            cost += edge.getCost();
        }
        if (travelTime > maxTravelTime || cost > maxBudget) {
            return null;
        }
        double score = computeObjectiveScore(distance, travelTime, cost, weights, maxTravelTime, maxBudget);
        return new RouteCandidate(chromosome, score, distance, travelTime, cost);
    }

    private double computeObjectiveScore(double distance, double travelTime, double totalCost,
            ObjectiveWeights weights, double maxTravelTime, double maxBudget) {
        double normalizedDistance = distance / Math.max(1.0, distance + 100.0);
        double normalizedTime = travelTime / Math.max(1.0, maxTravelTime);
        double normalizedCost = totalCost / Math.max(1.0, maxBudget);
        double weighted = (weights.distanceWeight() * normalizedDistance)
                + (weights.timeWeight() * normalizedTime)
                + (weights.costWeight() * normalizedCost);
        return 100.0 - (weighted * 100.0);
    }

    private Edge findEdge(TravelNetwork network, String source, String target) {
        for (Edge edge : network.getOutgoing(source)) {
            if (edge.getTargetName().equals(target)) {
                return edge;
            }
        }
        return null;
    }

    private RouteSolution failure(long startTime, String message) {
        return new RouteSolution(List.of(), 0.0, 0.0, 0.0, 0.0, durationMs(startTime), 0, 0, false, message);
    }

    private long durationMs(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    private record RouteCandidate(List<String> route, double score, double distance, double travelTime, double cost) {
        private RouteSolution toSolution(long executionTimeMs, int nodesExplored, int statesExplored) {
            return new RouteSolution(route, distance, travelTime, cost, score, executionTimeMs,
                    nodesExplored, statesExplored, true, null);
        }
    }
}
