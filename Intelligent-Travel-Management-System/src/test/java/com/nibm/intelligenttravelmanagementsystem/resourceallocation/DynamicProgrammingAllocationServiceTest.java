package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.*;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.DynamicProgrammingAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DynamicProgrammingAllocationServiceTest {

    private DynamicProgrammingAllocationService dpService;

    @BeforeEach
    void setUp() {
        dpService = new DynamicProgrammingAllocationService();
    }

    @Test
    @DisplayName("1. Correct Optimal Subproblem Result - Should find exact global optimum for 0-1 Knapsack subproblem")
    void testOptimalSubproblemResult() {
        // Classic 0-1 Knapsack test case:
        // Item 1: Cost 10, Value 60
        // Item 2: Cost 20, Value 100
        // Item 3: Cost 30, Value 120
        // Capacity: 50 -> Optimal subset is Item 2 + Item 3 (Cost 50, Value 220)
        ResourceOption item1 = ResourceOption.builder().id("1").name("Item 1").cost(10.0).usefulness(60.0).available(true).build();
        ResourceOption item2 = ResourceOption.builder().id("2").name("Item 2").cost(20.0).usefulness(100.0).available(true).build();
        ResourceOption item3 = ResourceOption.builder().id("3").name("Item 3").cost(30.0).usefulness(120.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(60.0)
                .emergencyReserve(10.0) // Effective budget = 50.0
                .maxAvailableHours(100.0)
                .maxCarryingCapacityKg(100.0)
                .candidateOptions(List.of(item1, item2, item3))
                .build();

        AllocationResult result = dpService.allocate(problem);

        assertTrue(result.isFeasible());
        assertEquals(220.0, result.getOverallScore());
        assertEquals(50.0, result.getTotalCost());
        assertFalse(result.getSelectedResources().contains(item1));
        assertTrue(result.getSelectedResources().contains(item2));
        assertTrue(result.getSelectedResources().contains(item3));
    }

    @Test
    @DisplayName("2. DP Reconstruction - Should correctly reconstruct selected items from 2D DP matrix backtrack")
    void testDPReconstruction() {
        ResourceOption itemA = ResourceOption.builder().id("A").name("Kit A").cost(15.0).usefulness(50.0).available(true).build();
        ResourceOption itemB = ResourceOption.builder().id("B").name("Kit B").cost(25.0).usefulness(90.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(50.0)
                .emergencyReserve(10.0) // Effective budget = 40.0
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(10.0)
                .candidateOptions(List.of(itemA, itemB))
                .build();

        AllocationResult result = dpService.allocate(problem);

        assertTrue(result.isFeasible());
        assertEquals(2, result.getSelectedResources().size());
        assertEquals(140.0, result.getOverallScore());
        assertEquals(40.0, result.getTotalCost());
    }

    @Test
    @DisplayName("3. Constrained Capacity/Budget - Should respect physical capacity and budget bounds simultaneously")
    void testConstrainedCapacityAndBudget() {
        ResourceOption heavy = ResourceOption.builder().id("1").name("Heavy").cost(30.0).weightKg(8.0).usefulness(95.0).available(true).build();
        ResourceOption compact = ResourceOption.builder().id("2").name("Compact").cost(30.0).weightKg(2.0).usefulness(80.0).available(true).build();
        ResourceOption light = ResourceOption.builder().id("3").name("Light").cost(20.0).weightKg(1.0).usefulness(70.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(100.0)
                .emergencyReserve(20.0) // Effective budget = 80.0
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(4.0) // Capacity restricts heavy (8kg)
                .candidateOptions(List.of(heavy, compact, light))
                .build();

        AllocationResult result = dpService.allocate(problem);

        assertTrue(result.isFeasible());
        assertFalse(result.getSelectedResources().contains(heavy));
        assertTrue(result.getSelectedResources().contains(compact));
        assertTrue(result.getSelectedResources().contains(light));
    }

    @Test
    @DisplayName("4. Infeasible Case - Should return infeasible when no items fit within effective budget")
    void testInfeasibleCase() {
        ResourceOption luxury = ResourceOption.builder().id("1").name("Luxury").cost(500.0).usefulness(100.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(100.0)
                .emergencyReserve(50.0) // Effective budget = 50.0
                .maxAvailableHours(5.0)
                .maxCarryingCapacityKg(5.0)
                .candidateOptions(List.of(luxury))
                .build();

        AllocationResult result = dpService.allocate(problem);

        assertFalse(result.isFeasible());
        assertTrue(result.getSelectedResources().isEmpty());
    }
}
