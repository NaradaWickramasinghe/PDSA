package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationProblem;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationResult;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceCategory;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GreedyAllocationService implements AllocationAlgorithm {

    private static final String ALGORITHM_NAME = "GREEDY";
    private static final double EPSILON = 1e-6;

    @Override
    public AllocationResult allocate(AllocationProblem problem) {
        long startTime = System.currentTimeMillis();

        if (problem == null || problem.getCandidateOptions() == null || problem.getCandidateOptions().isEmpty()) {
            return AllocationResult.infeasible(ALGORITHM_NAME, "No candidate resources provided for allocation.");
        }

        double effectiveBudget = problem.getEffectiveBudget();
        double maxTime = problem.getMaxAvailableHours();
        double maxCapacity = problem.getMaxCarryingCapacityKg();

        if (effectiveBudget <= 0 && maxTime <= 0 && maxCapacity <= 0) {
            return AllocationResult.infeasible(ALGORITHM_NAME, "Constraints permit no available allocation capacity.");
        }

        List<ResourceOption> candidates = new ArrayList<>();
        for (ResourceOption option : problem.getCandidateOptions()) {
            if (option != null && option.isAvailable()) {
                candidates.add(option);
            }
        }

        if (candidates.isEmpty()) {
            return AllocationResult.infeasible(ALGORITHM_NAME, "No available candidate resources found.");
        }

        candidates.sort(Comparator.comparingDouble((ResourceOption o) -> calculateEfficiency(o, problem)).reversed());

        List<ResourceOption> selectedResources = new ArrayList<>();
        double accumulatedCost = 0.0;
        double accumulatedTime = 0.0;
        double accumulatedWeight = 0.0;
        double totalScore = 0.0;

        boolean hasTransportCandidates = candidates.stream().anyMatch(c -> c.getCategory() == ResourceCategory.TRANSPORTATION);

        // Phase 1: Select 1 Transportation option if available
        ResourceOption bestTransport = candidates.stream()
                .filter(c -> c.getCategory() == ResourceCategory.TRANSPORTATION)
                .filter(c -> c.getCost() <= effectiveBudget + EPSILON
                          && c.getDurationHours() <= maxTime + EPSILON
                          && c.getWeightKg() <= maxCapacity + EPSILON)
                .findFirst()
                .orElse(null);

        if (bestTransport != null) {
            selectedResources.add(bestTransport);
            accumulatedCost += bestTransport.getCost();
            accumulatedTime += bestTransport.getDurationHours();
            accumulatedWeight += bestTransport.getWeightKg();
            totalScore += bestTransport.getUsefulness();
        }

        // Phase 2: Select 1 Accommodation option if available
        final double remBudget1 = effectiveBudget - accumulatedCost;
        final double remTime1 = maxTime - accumulatedTime;
        final double remWeight1 = maxCapacity - accumulatedWeight;

        ResourceOption bestAccommodation = candidates.stream()
                .filter(c -> c.getCategory() == ResourceCategory.ACCOMMODATION)
                .filter(c -> c.getCost() <= remBudget1 + EPSILON
                          && c.getDurationHours() <= remTime1 + EPSILON
                          && c.getWeightKg() <= remWeight1 + EPSILON)
                .findFirst()
                .orElse(null);

        if (bestAccommodation != null) {
            selectedResources.add(bestAccommodation);
            accumulatedCost += bestAccommodation.getCost();
            accumulatedTime += bestAccommodation.getDurationHours();
            accumulatedWeight += bestAccommodation.getWeightKg();
            totalScore += bestAccommodation.getUsefulness();
        }

        // Phase 3: Fill remaining budget/time/capacity with best remaining activities & equipment
        for (ResourceOption candidate : candidates) {
            if (selectedResources.contains(candidate)) {
                continue;
            }

            boolean respectsBudget = (accumulatedCost + candidate.getCost()) <= effectiveBudget + EPSILON;
            boolean respectsTime = (accumulatedTime + candidate.getDurationHours()) <= maxTime + EPSILON;
            boolean respectsCapacity = (accumulatedWeight + candidate.getWeightKg()) <= maxCapacity + EPSILON;

            if (respectsBudget && respectsTime && respectsCapacity) {
                selectedResources.add(candidate);
                accumulatedCost += candidate.getCost();
                accumulatedTime += candidate.getDurationHours();
                accumulatedWeight += candidate.getWeightKg();
                totalScore += candidate.getUsefulness();
            }
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;

        boolean isFeasible = !selectedResources.isEmpty() 
                && accumulatedCost <= effectiveBudget + EPSILON
                && (!hasTransportCandidates || bestTransport != null);

        if (!isFeasible) {
            selectedResources.clear();
            accumulatedCost = 0.0;
            accumulatedTime = 0.0;
            accumulatedWeight = 0.0;
            totalScore = 0.0;
        }

        String message = isFeasible 
                ? "Greedy travel resource plan generated successfully." 
                : "INFEASIBLE: Constraints or transportation coverage could not be satisfied.";

        return AllocationResult.builder()
                .algorithmName(ALGORITHM_NAME)
                .feasible(isFeasible)
                .selectedResources(selectedResources)
                .totalCost(accumulatedCost)
                .remainingBudget(Math.max(0.0, effectiveBudget - accumulatedCost))
                .totalTime(accumulatedTime)
                .remainingTime(Math.max(0.0, maxTime - accumulatedTime))
                .totalWeight(accumulatedWeight)
                .remainingCapacity(Math.max(0.0, maxCapacity - accumulatedWeight))
                .overallScore(totalScore)
                .executionTimeMs(executionTimeMs)
                .statusMessage(message)
                .build();
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    private double calculateEfficiency(ResourceOption option, AllocationProblem problem) {
        double effectiveBudget = problem.getEffectiveBudget();
        double maxTime = problem.getMaxAvailableHours();
        double maxCapacity = problem.getMaxCarryingCapacityKg();

        double normCost = effectiveBudget > 0 ? (option.getCost() / effectiveBudget) : 0.0;
        double normTime = maxTime > 0 ? (option.getDurationHours() / maxTime) : 0.0;
        double normWeight = maxCapacity > 0 ? (option.getWeightKg() / maxCapacity) : 0.0;

        double totalNormalizedConsumption = normCost + normTime + normWeight;
        if (totalNormalizedConsumption <= 0) {
            return option.getUsefulness();
        }

        return option.getUsefulness() / totalNormalizedConsumption;
    }

    /**
     * Pipeline Stage 1: Greedy Transportation Allocation using PriorityQueue.
     * Evaluates cost, travel time, traveller capacity, and availability dynamically.
     */
    public ResourceOption allocateTransportation(AllocationProblem problem, double availableBudget, double availableTime, double availableWeight, int travellerCount) {
        if (problem == null || problem.getCandidateOptions() == null) {
            return null;
        }

        List<ResourceOption> transportCandidates = problem.getCandidateOptions().stream()
                .filter(o -> o != null && o.isAvailable() && o.getCategory() == ResourceCategory.TRANSPORTATION)
                .filter(o -> o.getCapacity() == null || o.getCapacity() >= travellerCount)
                .toList();

        if (transportCandidates.isEmpty()) {
            return null;
        }

        // PriorityQueue prioritizing maximum usefulness per combined resource footprint
        java.util.PriorityQueue<ResourceOption> pq = new java.util.PriorityQueue<>((a, b) -> {
            double priorityA = calculateTransportPriority(a, availableBudget, availableTime);
            double priorityB = calculateTransportPriority(b, availableBudget, availableTime);
            return Double.compare(priorityB, priorityA); // descending order
        });

        for (ResourceOption candidate : transportCandidates) {
            pq.offer(candidate);
        }

        while (!pq.isEmpty()) {
            ResourceOption candidate = pq.poll();
            boolean fitsBudget = candidate.getCost() <= availableBudget + EPSILON;
            boolean fitsTime = candidate.getDurationHours() <= availableTime + EPSILON;
            boolean fitsWeight = candidate.getWeightKg() <= availableWeight + EPSILON;

            if (fitsBudget && fitsTime && fitsWeight) {
                return candidate;
            }
        }

        return null;
    }

    private double calculateTransportPriority(ResourceOption opt, double budget, double time) {
        double normCost = budget > 0 ? (opt.getCost() / budget) : 1.0;
        double normTime = time > 0 ? (opt.getDurationHours() / time) : 1.0;
        double totalConsumption = (normCost * 0.6) + (normTime * 0.4) + 0.05;
        return opt.getUsefulness() / totalConsumption;
    }
}
