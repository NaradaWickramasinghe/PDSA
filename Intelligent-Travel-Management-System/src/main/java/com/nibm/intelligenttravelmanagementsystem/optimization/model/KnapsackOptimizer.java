package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 0/1 Knapsack Dynamic Programming Optimizer.
 *
 * Direct Syllabus Topics:
 * - "0/1 knapsak", "Knapsack Problem"
 * - "Linked lists stacks" (Backtracking selection using Stack)
 * - "Big o notation" (O(N * W) time complexity, O(N * W) space complexity)
 *
 * Formulation:
 * Each candidate destination from Module 4 + Module 3 + Module 1 + Module 2 represents an item:
 * - Weight: Cost (discretized into scale units)
 * - Value: Multi-Objective Utility score
 * - Capacity: User's Max Budget Limit
 *
 * DP Recurrence Relation:
 * dp[i][w] = dp[i-1][w]                                    if weight[i] > w
 * dp[i][w] = max(dp[i-1][w], dp[i-1][w - weight[i]] + value[i]) otherwise
 */
@Component
public class KnapsackOptimizer {

    public String getName() {
        return "KNAPSACK_DYNAMIC_PROGRAMMING";
    }

    public static class KnapsackResult {
        private final List<IntegratedCandidate> selectedCandidates;
        private final double totalUtility;
        private final double totalCost;
        private final double totalDurationMinutes;
        private final double executionTimeMs;
        private final double memoryUsedKb;
        private final int statesEvaluated;

        public KnapsackResult(List<IntegratedCandidate> selectedCandidates, double totalUtility,
                              double totalCost, double totalDurationMinutes,
                              double executionTimeMs, double memoryUsedKb, int statesEvaluated) {
            this.selectedCandidates = selectedCandidates;
            this.totalUtility = totalUtility;
            this.totalCost = totalCost;
            this.totalDurationMinutes = totalDurationMinutes;
            this.executionTimeMs = executionTimeMs;
            this.memoryUsedKb = memoryUsedKb;
            this.statesEvaluated = statesEvaluated;
        }

        public List<IntegratedCandidate> getSelectedCandidates() { return selectedCandidates; }
        public double getTotalUtility() { return totalUtility; }
        public double getTotalCost() { return totalCost; }
        public double getTotalDurationMinutes() { return totalDurationMinutes; }
        public double getExecutionTimeMs() { return executionTimeMs; }
        public double getMemoryUsedKb() { return memoryUsedKb; }
        public int getStatesEvaluated() { return statesEvaluated; }
    }

    /**
     * Solves the 0/1 Knapsack problem for itinerary destination selection.
     *
     * @param candidates Items collected from Modules 1-4
     * @param request    User budget and time constraints
     * @return KnapsackResult containing optimal selection
     */
    public KnapsackResult solve(List<IntegratedCandidate> candidates, OptimizationRequest request) {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        if (candidates == null || candidates.isEmpty()) {
            return new KnapsackResult(Collections.emptyList(), 0, 0, 0, 0, 0, 0);
        }

        int n = candidates.size();
        double maxBudget = (request.getMaxBudgetLkr() != null && request.getMaxBudgetLkr() > 0)
                ? request.getMaxBudgetLkr()
                : 35000.0;
        double maxTime = (request.getMaxTimeMinutes() != null && request.getMaxTimeMinutes() > 0)
                ? request.getMaxTimeMinutes()
                : 720.0;

        // Discretization step to create a manageable integer capacity for DP table (e.g. 500 LKR per unit)
        int stepSize = 500;
        int capacity = Math.max(1, (int) Math.floor(maxBudget / stepSize));

        int[] weights = new int[n + 1];
        double[] values = new double[n + 1];

        for (int i = 0; i < n; i++) {
            IntegratedCandidate c = candidates.get(i);
            int w = (int) Math.ceil(c.getCompositeCost() / stepSize);
            weights[i + 1] = Math.max(1, w);
            values[i + 1] = c.getCompositeValue();
        }

        // 2D DP Table: dp[i][w] stores maximum utility using first i candidates with budget capacity w
        double[][] dp = new double[n + 1][capacity + 1];

        // Fill DP table
        int states = 0;
        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= capacity; w++) {
                states++;
                if (weights[i] <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - weights[i]] + values[i]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        // Backtrack using a Stack to find which items were selected (Syllabus topic: "Linked lists stacks")
        Deque<Integer> selectionStack = new ArrayDeque<>();
        int remainingCapacity = capacity;
        for (int i = n; i > 0; i--) {
            if (dp[i][remainingCapacity] != dp[i - 1][remainingCapacity]) {
                selectionStack.push(i - 1);
                remainingCapacity -= weights[i];
            }
        }

        // Pop elements from Stack in order
        List<IntegratedCandidate> selected = new ArrayList<>();
        double totalCost = 0.0;
        double totalDuration = 0.0;
        double totalUtility = 0.0;

        while (!selectionStack.isEmpty()) {
            int idx = selectionStack.pop();
            IntegratedCandidate c = candidates.get(idx);
            // Verify time constraint isn't violated
            if (totalDuration + c.getCompositeTimeMinutes() <= maxTime || selected.isEmpty()) {
                selected.add(c);
                totalCost += c.getCompositeCost();
                totalDuration += c.getCompositeTimeMinutes();
                totalUtility += c.getCompositeValue();
            }
        }

        long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double executionTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
        double memoryUsedKb = Math.max(0.0, (endMem - startMem) / 1024.0);

        return new KnapsackResult(
                selected,
                Math.round(totalUtility * 1000.0) / 1000.0,
                Math.round(totalCost),
                Math.round(totalDuration),
                executionTimeMs,
                memoryUsedKb,
                states
        );
    }
}
