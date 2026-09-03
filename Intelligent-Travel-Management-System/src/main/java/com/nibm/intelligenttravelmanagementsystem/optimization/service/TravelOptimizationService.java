package com.nibm.intelligenttravelmanagementsystem.optimization.service;

import com.nibm.intelligenttravelmanagementsystem.optimization.algorithm.BranchAndBoundOptimizer;
import com.nibm.intelligenttravelmanagementsystem.optimization.algorithm.GeneticAlgorithmOptimizer;
import com.nibm.intelligenttravelmanagementsystem.optimization.algorithm.ParetoDynamicProgrammingOptimizer;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.BenchmarkSummary;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.ObjectiveWeights;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationResponse;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.RouteSolution;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.TravelNetwork;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.TravelNetworkBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TravelOptimizationService {
    private static final String BRANCH_AND_BOUND = "BRANCH_AND_BOUND";
    private static final String GENETIC_ALGORITHM = "GENETIC_ALGORITHM";
    private static final String PARETO_FRONTIER = "PARETO_FRONTIER";
    private static final String BENCHMARK = "BENCHMARK";

    private final TravelNetwork network = TravelNetworkBuilder.buildDefaultNetwork();

    public OptimizationResponse optimize(OptimizationRequest request) {
        validateRequest(request);
        String algorithm = normalizeMethod(request.getOptimizationMethod());
        ObjectiveWeights weights = request.getObjectiveWeights() != null ? request.getObjectiveWeights()
                : ObjectiveWeights.defaults();

        RouteSolution solution;
        switch (algorithm) {
            case GENETIC_ALGORITHM -> solution = new GeneticAlgorithmOptimizer().optimize(
                    network, request.getOrigin(), request.getDestination(),
                    request.getMaxTravelTime(), request.getMaxBudget(), weights);
            case PARETO_FRONTIER -> solution = new ParetoDynamicProgrammingOptimizer().optimize(
                    network, request.getOrigin(), request.getDestination(),
                    request.getMaxTravelTime(), request.getMaxBudget(), weights);
            case BENCHMARK -> {
                BenchmarkSummary summary = benchmark(request);
                OptimizationResponse benchmarkLeader = summary.getResults().stream()
                        .filter(OptimizationResponse::isSuccess)
                        .max(Comparator.comparingDouble(OptimizationResponse::getObjectiveScore))
                        .orElse(summary.getResults().get(0));
                return benchmarkLeader;
            }
            default -> solution = new BranchAndBoundOptimizer().optimize(
                    network, request.getOrigin(), request.getDestination(),
                    request.getMaxTravelTime(), request.getMaxBudget(), weights);
        }

        return mapResponse(solution, algorithm);
    }

    public BenchmarkSummary benchmark(OptimizationRequest request) {
        validateRequest(request);
        ObjectiveWeights weights = request.getObjectiveWeights() != null ? request.getObjectiveWeights()
                : ObjectiveWeights.defaults();
        OptimizationRequest branchRequest = copyRequest(request, BRANCH_AND_BOUND, weights);
        OptimizationRequest geneticRequest = copyRequest(request, GENETIC_ALGORITHM, weights);
        OptimizationRequest paretoRequest = copyRequest(request, PARETO_FRONTIER, weights);

        List<OptimizationResponse> results = new ArrayList<>();
        results.add(optimize(branchRequest));
        results.add(optimize(geneticRequest));
        results.add(optimize(paretoRequest));

        OptimizationResponse best = results.stream()
                .filter(OptimizationResponse::isSuccess)
                .max(Comparator.comparingDouble(OptimizationResponse::getObjectiveScore))
                .orElse(results.get(0));

        return new BenchmarkSummary(results, best.getSelectedAlgorithm(), best.getObjectiveScore());
    }

    private OptimizationRequest copyRequest(OptimizationRequest request, String algorithm, ObjectiveWeights weights) {
        OptimizationRequest copy = new OptimizationRequest();
        copy.setOrigin(request.getOrigin());
        copy.setDestination(request.getDestination());
        copy.setMaxTravelTime(request.getMaxTravelTime());
        copy.setMaxBudget(request.getMaxBudget());
        copy.setOptimizationMethod(algorithm);
        copy.setObjectiveWeights(weights);
        return copy;
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return BRANCH_AND_BOUND;
        }
        String normalized = method.trim().toUpperCase().replace("-", "_").replace(" ", "_");

        if (normalized.equals("GA") || normalized.equals("GENETIC") || normalized.equals("GENETIC_ALGORITHM")) {
            return GENETIC_ALGORITHM;
        }
        if (normalized.equals("PARETO") || normalized.equals("PARETO_DYNAMIC_PROGRAMMING")
                || normalized.equals("PARETO_FRONTIER")) {
            return PARETO_FRONTIER;
        }
        if (normalized.equals("BENCHMARK") || normalized.equals("COMPARE")) {
            return BENCHMARK;
        }
        if (normalized.equals("BRANCHANDBOUND") || normalized.equals("BRANCH_AND_BOUND")) {
            return BRANCH_AND_BOUND;
        }
        return BRANCH_AND_BOUND;
    }

    private void validateRequest(OptimizationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Optimization request cannot be null.");
        }
        if (request.getOrigin() == null || request.getOrigin().isBlank()) {
            throw new IllegalArgumentException("Origin must be provided.");
        }
        if (request.getDestination() == null || request.getDestination().isBlank()) {
            throw new IllegalArgumentException("Destination must be provided.");
        }
        if (request.getMaxTravelTime() == null || request.getMaxTravelTime() <= 0) {
            throw new IllegalArgumentException("Maximum travel time must be a positive number.");
        }
        if (request.getMaxBudget() == null || request.getMaxBudget() <= 0) {
            throw new IllegalArgumentException("Maximum budget must be a positive number.");
        }
    }

    private OptimizationResponse mapResponse(RouteSolution solution, String algorithm) {
        if (solution == null) {
            return new OptimizationResponse(algorithm, List.of(), 0.0, 0.0, 0.0, 0.0, 0L, 0, 0, false,
                    "Optimization returned no result.");
        }
        return new OptimizationResponse(
                algorithm,
                solution.getRoute(),
                solution.getTotalDistance(),
                solution.getTotalTravelTime(),
                solution.getTotalCost(),
                solution.getObjectiveScore(),
                solution.getExecutionTimeMs(),
                solution.getNodesExplored(),
                solution.getStatesExplored(),
                solution.isSuccess(),
                solution.getErrorMessage());
    }
}
