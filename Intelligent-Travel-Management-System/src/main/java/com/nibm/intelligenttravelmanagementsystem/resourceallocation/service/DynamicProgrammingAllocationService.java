package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationProblem;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationResult;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceCategory;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class DynamicProgrammingAllocationService implements AllocationAlgorithm {

    private static final String ALGORITHM_NAME = "DYNAMIC_PROGRAMMING";
    private static final double EPSILON = 1e-6;
    private static final int MAX_DP_TABLE_CAPACITY = 10000;

    @Override
    public AllocationResult allocate(AllocationProblem problem) {
        long startTime = System.currentTimeMillis();

        if (problem == null || problem.getCandidateOptions() == null || problem.getCandidateOptions().isEmpty()) {
            return AllocationResult.infeasible(ALGORITHM_NAME, "No candidate resources provided for DP allocation.");
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

        List<ResourceOption> selectedResources = new ArrayList<>();
        double accumulatedCost = 0.0;
        double accumulatedTime = 0.0;
        double accumulatedWeight = 0.0;

        boolean hasTransportCandidates = candidates.stream().anyMatch(c -> c.getCategory() == ResourceCategory.TRANSPORTATION);
        boolean hasAccommodationCandidates = candidates.stream().anyMatch(c -> c.getCategory() == ResourceCategory.ACCOMMODATION);

        // Phase 1: Select best Transportation option if available
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
        }

        // Phase 2: Select best Accommodation option if available
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
        }

        // Phase 3: Run DP 0/1 Knapsack on remaining activities & equipment for remaining capacity
        List<ResourceOption> dpCandidates = new ArrayList<>();
        for (ResourceOption c : candidates) {
            if (!selectedResources.contains(c)) {
                dpCandidates.add(c);
            }
        }

        double remBudget = Math.max(0.0, effectiveBudget - accumulatedCost);
        double remTime = Math.max(0.0, maxTime - accumulatedTime);
        double remWeight = Math.max(0.0, maxCapacity - accumulatedWeight);

        int n = dpCandidates.size();
        int budgetCap = Math.min((int) Math.floor(remBudget), MAX_DP_TABLE_CAPACITY);

        if (n > 0 && budgetCap > 0) {
            double[][] dp = new double[n + 1][budgetCap + 1];

            for (int i = 1; i <= n; i++) {
                ResourceOption current = dpCandidates.get(i - 1);
                int costInt = (int) Math.ceil(current.getCost());

                for (int w = 0; w <= budgetCap; w++) {
                    dp[i][w] = dp[i - 1][w];

                    if (costInt <= w) {
                        double potentialScore = dp[i - 1][w - costInt] + current.getUsefulness();
                        if (potentialScore > dp[i][w] && isValidSecondaryConstraints(dpCandidates, i - 1, w - costInt, current, remTime, remWeight, dp)) {
                            dp[i][w] = potentialScore;
                        }
                    }
                }
            }

            int currW = budgetCap;
            List<ResourceOption> dpSelected = new ArrayList<>();
            for (int i = n; i > 0; i--) {
                ResourceOption current = dpCandidates.get(i - 1);
                int costInt = (int) Math.ceil(current.getCost());

                if (currW >= costInt && dp[i][currW] > dp[i - 1][currW] + EPSILON) {
                    dpSelected.add(current);
                    currW -= costInt;
                }
            }

            Collections.reverse(dpSelected);
            selectedResources.addAll(dpSelected);
        }

        // Final metric recalculation
        double totalCost = 0.0;
        double totalTime = 0.0;
        double totalWeight = 0.0;
        double overallScore = 0.0;

        for (ResourceOption item : selectedResources) {
            totalCost += item.getCost();
            totalTime += item.getDurationHours();
            totalWeight += item.getWeightKg();
            overallScore += item.getUsefulness();
        }

        boolean selectedTransport = selectedResources.stream().anyMatch(c -> c.getCategory() == ResourceCategory.TRANSPORTATION);
        boolean selectedAccommodation = selectedResources.stream().anyMatch(c -> c.getCategory() == ResourceCategory.ACCOMMODATION);

        boolean isFeasible = !selectedResources.isEmpty() 
                && (totalCost <= effectiveBudget + EPSILON)
                && (totalTime <= maxTime + EPSILON)
                && (totalWeight <= maxCapacity + EPSILON)
                && (!hasTransportCandidates || selectedTransport)
                && (!hasAccommodationCandidates || selectedAccommodation);

        if (!isFeasible) {
            selectedResources.clear();
            totalCost = 0.0;
            totalTime = 0.0;
            totalWeight = 0.0;
            overallScore = 0.0;
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;
        String message = isFeasible 
                ? "Dynamic Programming travel resource plan generated successfully." 
                : "INFEASIBLE: Constraints or complete travel plan role requirements could not be satisfied.";

        return AllocationResult.builder()
                .algorithmName(ALGORITHM_NAME)
                .feasible(isFeasible)
                .selectedResources(selectedResources)
                .totalCost(totalCost)
                .remainingBudget(Math.max(0.0, effectiveBudget - totalCost))
                .totalTime(totalTime)
                .remainingTime(Math.max(0.0, maxTime - totalTime))
                .totalWeight(totalWeight)
                .remainingCapacity(Math.max(0.0, maxCapacity - totalWeight))
                .overallScore(overallScore)
                .executionTimeMs(executionTimeMs)
                .statusMessage(message)
                .build();
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    private boolean isValidSecondaryConstraints(List<ResourceOption> candidates, int currentIndex, int remainingBudget,
                                                ResourceOption candidateToAdd, double maxRemTime, double maxRemWeight, double[][] dp) {
        double totalTime = candidateToAdd.getDurationHours();
        double totalWeight = candidateToAdd.getWeightKg();

        int w = remainingBudget;
        for (int i = currentIndex; i > 0; i--) {
            ResourceOption item = candidates.get(i - 1);
            int costInt = (int) Math.ceil(item.getCost());
            if (w >= costInt && dp[i][w] > dp[i - 1][w] + EPSILON) {
                totalTime += item.getDurationHours();
                totalWeight += item.getWeightKg();
                w -= costInt;
            }
        }

        return (totalTime <= maxRemTime + EPSILON) && (totalWeight <= maxRemWeight + EPSILON);
    }

    /**
     * Pipeline Stage 2: Equipment / Physical Resource Allocation using 0/1 Knapsack DP.
     * Builds state table dp[n+1][W+1] and reconstructs the optimal set of gear
     * constrained by luggage capacity and equipment budget allowance.
     */
    public List<ResourceOption> allocateEquipment(AllocationProblem problem, double availableBudget, double availableCapacityKg) {
        if (problem == null || problem.getCandidateOptions() == null || availableCapacityKg <= 0 || availableBudget <= 0) {
            return Collections.emptyList();
        }

        List<ResourceOption> equipmentCandidates = problem.getCandidateOptions().stream()
                .filter(o -> o != null && o.isAvailable() && (o.getCategory() == ResourceCategory.PHYSICAL_ITEM || (o.getId() != null && o.getId().startsWith("EQ"))))
                .filter(o -> o.getWeightKg() > 0 && o.getWeightKg() <= availableCapacityKg + EPSILON)
                .filter(o -> o.getCost() <= availableBudget + EPSILON)
                .toList();

        int n = equipmentCandidates.size();
        if (n == 0) {
            return Collections.emptyList();
        }

        // Scale weight to discrete decagram units (0.1 kg = 1 unit)
        final int SCALE = 10;
        int maxCapacityUnits = Math.min(1000, (int) Math.floor(availableCapacityKg * SCALE));
        if (maxCapacityUnits <= 0) {
            return Collections.emptyList();
        }

        // 0/1 Knapsack DP State Table: dp[i][w] = maximum usefulness using first i items within weight capacity w
        double[][] dp = new double[n + 1][maxCapacityUnits + 1];

        for (int i = 1; i <= n; i++) {
            ResourceOption item = equipmentCandidates.get(i - 1);
            int itemWeightUnits = Math.max(1, (int) Math.round(item.getWeightKg() * SCALE));
            double itemValue = item.getUsefulness();

            for (int w = 0; w <= maxCapacityUnits; w++) {
                dp[i][w] = dp[i - 1][w];
                if (itemWeightUnits <= w) {
                    double valueWithItem = dp[i - 1][w - itemWeightUnits] + itemValue;
                    if (valueWithItem > dp[i][w]) {
                        dp[i][w] = valueWithItem;
                    }
                }
            }
        }

        // DP Reconstruction: Backtrack from dp[n][maxCapacityUnits] to recover selected items
        List<ResourceOption> selectedEquipment = new ArrayList<>();
        int currWeightUnits = maxCapacityUnits;
        for (int i = n; i > 0; i--) {
            if (dp[i][currWeightUnits] > dp[i - 1][currWeightUnits] + EPSILON) {
                ResourceOption selectedItem = equipmentCandidates.get(i - 1);
                selectedEquipment.add(selectedItem);
                currWeightUnits -= Math.max(1, (int) Math.round(selectedItem.getWeightKg() * SCALE));
            }
        }

        Collections.reverse(selectedEquipment);

        // Budget envelope check: ensure total cost of selected equipment fits available budget
        double totalEquipCost = selectedEquipment.stream().mapToDouble(ResourceOption::getCost).sum();
        while (totalEquipCost > availableBudget && !selectedEquipment.isEmpty()) {
            ResourceOption leastEfficient = Collections.min(selectedEquipment,
                    Comparator.comparingDouble(o -> o.getUsefulness() / Math.max(1.0, o.getCost())));
            selectedEquipment.remove(leastEfficient);
            totalEquipCost = selectedEquipment.stream().mapToDouble(ResourceOption::getCost).sum();
        }

        return selectedEquipment;
    }
}
