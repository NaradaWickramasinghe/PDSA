package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.*;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.GeneticAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneticAllocationServiceTest {

    private GeneticAllocationService geneticService;

    @BeforeEach
    void setUp() {
        // Seeded random for deterministic behavior test
        geneticService = new GeneticAllocationService(50, 100, 0.05, 42L);
    }

    @Test
    @DisplayName("1. Valid Population - Should generate initial valid population and evolve feasible solution")
    void testValidPopulation() {
        ResourceOption item1 = ResourceOption.builder().id("1").name("Kit").cost(20.0).durationHours(1.0).weightKg(1.0).usefulness(90.0).available(true).build();
        ResourceOption item2 = ResourceOption.builder().id("2").name("Tour").cost(40.0).durationHours(2.0).weightKg(0.0).usefulness(80.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(200.0)
                .emergencyReserve(50.0)
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(5.0)
                .candidateOptions(List.of(item1, item2))
                .build();

        AllocationResult result = geneticService.allocate(problem);

        assertTrue(result.isFeasible());
        assertEquals("GENETIC", result.getAlgorithmName());
        assertFalse(result.getSelectedResources().isEmpty());
    }

    @Test
    @DisplayName("2. Fitness Evaluation & Penalty Function - Should penalize constraint violations")
    void testFitnessEvaluationAndPenalties() {
        ResourceOption expensive = ResourceOption.builder().id("1").name("Helicopter").cost(300.0).durationHours(1.0).weightKg(0.0).usefulness(99.0).available(true).build();
        ResourceOption affordable = ResourceOption.builder().id("2").name("Walking").cost(30.0).durationHours(2.0).weightKg(0.0).usefulness(60.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(150.0)
                .emergencyReserve(50.0) // Effective budget = 100.0
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(10.0)
                .candidateOptions(List.of(expensive, affordable))
                .build();

        AllocationResult result = geneticService.allocate(problem);

        assertTrue(result.isFeasible());
        assertFalse(result.getSelectedResources().contains(expensive));
        assertTrue(result.getTotalCost() <= 100.0);
    }

    @Test
    @DisplayName("3. Crossover & Mutation Behavior - Evolutionary iterations should maintain or improve solution fitness")
    void testCrossoverAndMutationBehavior() {
        List<ResourceOption> options = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            options.add(ResourceOption.builder()
                    .id(String.valueOf(i))
                    .name("Item " + i)
                    .cost(10.0 * i)
                    .durationHours(0.5 * i)
                    .weightKg(0.2 * i)
                    .usefulness(15.0 * i)
                    .available(true)
                    .build());
        }

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(200.0)
                .emergencyReserve(20.0)
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(5.0)
                .candidateOptions(options)
                .build();

        GeneticAllocationService shortRun = new GeneticAllocationService(20, 2, 0.05, 99L);
        GeneticAllocationService longRun = new GeneticAllocationService(50, 100, 0.05, 99L);

        AllocationResult shortResult = shortRun.allocate(problem);
        AllocationResult longResult = longRun.allocate(problem);

        assertTrue(longResult.isFeasible());
        assertTrue(longResult.getOverallScore() >= shortResult.getOverallScore());
    }

    @Test
    @DisplayName("4. Constraint Handling - Should handle zero capacity or zero time limits gracefully")
    void testConstraintHandling() {
        ResourceOption item = ResourceOption.builder().id("1").name("Heavy").cost(10.0).weightKg(10.0).usefulness(90.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(100.0)
                .emergencyReserve(20.0)
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(1.0) // Only 1kg capacity
                .candidateOptions(List.of(item))
                .build();

        AllocationResult result = geneticService.allocate(problem);

        assertFalse(result.isFeasible());
        assertTrue(result.getSelectedResources().isEmpty());
    }

    @Test
    @DisplayName("5. Reproducibility when Seeded - Identical seed must yield identical outputs")
    void testReproducibilityWhenSeeded() {
        ResourceOption item1 = ResourceOption.builder().id("1").name("A").cost(10.0).durationHours(1.0).weightKg(1.0).usefulness(50.0).build();
        ResourceOption item2 = ResourceOption.builder().id("2").name("B").cost(20.0).durationHours(2.0).weightKg(1.0).usefulness(70.0).build();
        ResourceOption item3 = ResourceOption.builder().id("3").name("C").cost(30.0).durationHours(3.0).weightKg(1.0).usefulness(90.0).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(100.0)
                .emergencyReserve(20.0)
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(10.0)
                .candidateOptions(List.of(item1, item2, item3))
                .build();

        GeneticAllocationService service1 = new GeneticAllocationService(50, 50, 0.05, 777L);
        GeneticAllocationService service2 = new GeneticAllocationService(50, 50, 0.05, 777L);

        AllocationResult result1 = service1.allocate(problem);
        AllocationResult result2 = service2.allocate(problem);

        assertEquals(result1.isFeasible(), result2.isFeasible());
        assertEquals(result1.getOverallScore(), result2.getOverallScore());
        assertEquals(result1.getSelectedResources().size(), result2.getSelectedResources().size());
    }
}
