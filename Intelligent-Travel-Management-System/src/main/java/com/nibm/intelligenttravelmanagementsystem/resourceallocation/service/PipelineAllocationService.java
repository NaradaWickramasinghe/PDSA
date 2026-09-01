package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationProblem;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationResult;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PipelineAllocationService implements AllocationAlgorithm {

    public static final String ALGORITHM_NAME = "PIPELINE";
    private static final double EPSILON = 1e-6;

    private final GreedyAllocationService greedyService;
    private final DynamicProgrammingAllocationService dpService;
    private final GeneticAllocationService geneticService;

    public PipelineAllocationService(GreedyAllocationService greedyService,
                                    DynamicProgrammingAllocationService dpService,
                                    GeneticAllocationService geneticService) {
        this.greedyService = greedyService != null ? greedyService : new GreedyAllocationService();
        this.dpService = dpService != null ? dpService : new DynamicProgrammingAllocationService();
        this.geneticService = geneticService != null ? geneticService : new GeneticAllocationService();
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    @Override
    public AllocationResult allocate(AllocationProblem problem) {
        long startTime = System.currentTimeMillis();

        if (problem == null || problem.getCandidateOptions() == null || problem.getCandidateOptions().isEmpty()) {
            return AllocationResult.infeasible(ALGORITHM_NAME, "No candidate resources provided for allocation pipeline.");
        }

        double effectiveBudget = problem.getEffectiveBudget();
        double availableHours = problem.getMaxAvailableHours();
        double luggageCapacity = problem.getMaxCarryingCapacityKg();
        int travellers = (problem.getTravellerCount() != null && problem.getTravellerCount() > 0) ? problem.getTravellerCount() : 1;
        int durationDays = (problem.getTripDurationDays() != null && problem.getTripDurationDays() > 0) ? problem.getTripDurationDays() : 3;

        if (effectiveBudget <= 0 && availableHours <= 0 && luggageCapacity <= 0) {
            return AllocationResult.infeasible(ALGORITHM_NAME, "Constraints permit no available allocation capacity.");
        }

        double remainingBudget = effectiveBudget;
        double remainingHours = availableHours;
        double remainingCapacity = luggageCapacity;

        List<ResourceOption> allSelectedResources = new ArrayList<>();
        double accumulatedScore = 0.0;

        // =========================================================================
        // STAGE 1: GREEDY ALGORITHM — Transportation Allocation
        // PriorityQueue-based selection considering cost, time, and traveller capacity
        // =========================================================================
        ResourceOption selectedTransport = greedyService.allocateTransportation(
                problem,
                remainingBudget,
                remainingHours,
                remainingCapacity,
                travellers
        );

        if (selectedTransport != null) {
            allSelectedResources.add(selectedTransport);
            remainingBudget -= selectedTransport.getCost();
            remainingHours = Math.max(0.0, remainingHours - selectedTransport.getDurationHours());
            remainingCapacity = Math.max(0.0, remainingCapacity - selectedTransport.getWeightKg());
            accumulatedScore += selectedTransport.getUsefulness();
        }

        // =========================================================================
        // STAGE 2: DYNAMIC PROGRAMMING — Equipment / Physical Resource Allocation
        // Genuine 0/1 Knapsack optimization on luggage carrying capacity
        // =========================================================================
        double equipBudgetCap = Math.min(Math.max(3000.0, remainingBudget * 0.20), 12000.0);
        List<ResourceOption> selectedEquipment = dpService.allocateEquipment(
                problem,
                equipBudgetCap,
                remainingCapacity
        );

        if (selectedEquipment != null && !selectedEquipment.isEmpty()) {
            for (ResourceOption eq : selectedEquipment) {
                allSelectedResources.add(eq);
                remainingBudget -= eq.getCost();
                remainingCapacity = Math.max(0.0, remainingCapacity - eq.getWeightKg());
                accumulatedScore += eq.getUsefulness();
            }
        }

        // =========================================================================
        // STAGE 3: GENETIC ALGORITHM — Experience Optimization (Accommodation + Activities)
        // Multi-objective Pareto search over remaining budget and daylight hours
        // =========================================================================
        GeneticAllocationService.GeneticStageResult gaResult = geneticService.optimizeAccommodationAndActivities(
                problem,
                remainingBudget,
                remainingHours,
                travellers,
                durationDays
        );

        if (gaResult != null) {
            // Selected Accommodation (for multi-day trips)
            if (gaResult.getSelectedAccommodation() != null) {
                ResourceOption acc = gaResult.getSelectedAccommodation();
                allSelectedResources.add(acc);
                int nights = durationDays > 1 ? Math.max(1, durationDays - 1) : 0;
                double accCost = acc.getCost() * (nights > 0 ? nights : 1);
                remainingBudget -= accCost;
                accumulatedScore += acc.getUsefulness();
            }

            // Selected Activities
            if (gaResult.getSelectedActivities() != null) {
                for (ResourceOption act : gaResult.getSelectedActivities()) {
                    allSelectedResources.add(act);
                    remainingBudget -= act.getCost();
                    remainingHours = Math.max(0.0, remainingHours - act.getDurationHours());
                    accumulatedScore += act.getUsefulness();
                }
            }
        }

        // =========================================================================
        // PIPELINE AGGREGATION & INVARIANT RECONCILIATION
        // =========================================================================
        double totalCost = effectiveBudget - remainingBudget;
        double totalTime = availableHours - remainingHours;
        double totalWeight = luggageCapacity - remainingCapacity;

        long executionTimeMs = System.currentTimeMillis() - startTime;
        boolean isFeasible = !allSelectedResources.isEmpty() && (effectiveBudget - totalCost >= -EPSILON);

        String message = isFeasible
                ? "Pipeline Travel Resource Plan synthesized successfully across Greedy (Transport), Dynamic Programming (Equipment), and Genetic Algorithm (Accommodation & Activities)."
                : "INFEASIBLE: Pipeline could not satisfy hard budget or time limits.";

        return AllocationResult.builder()
                .algorithmName(ALGORITHM_NAME)
                .feasible(isFeasible)
                .selectedResources(allSelectedResources)
                .totalCost(Math.max(0.0, totalCost))
                .remainingBudget(Math.max(0.0, remainingBudget))
                .totalTime(Math.max(0.0, totalTime))
                .remainingTime(Math.max(0.0, remainingHours))
                .totalWeight(Math.max(0.0, totalWeight))
                .remainingCapacity(Math.max(0.0, remainingCapacity))
                .overallScore(accumulatedScore)
                .executionTimeMs(executionTimeMs)
                .statusMessage(message)
                .build();
    }
}
