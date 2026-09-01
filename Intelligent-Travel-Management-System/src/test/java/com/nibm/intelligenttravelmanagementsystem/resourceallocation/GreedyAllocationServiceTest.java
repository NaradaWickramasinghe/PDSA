package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.*;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.GreedyAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GreedyAllocationServiceTest {

    private GreedyAllocationService greedyService;

    @BeforeEach
    void setUp() {
        greedyService = new GreedyAllocationService();
    }

    @Test
    @DisplayName("1. Priority Order - Should select candidate with higher efficiency ratio first")
    void testPriorityOrder() {
        // Item A: Usefulness 90, Cost 10 (Efficiency 90/10 = 9)
        ResourceOption itemA = ResourceOption.builder()
                .id("A").name("High Efficiency").category(ResourceCategory.PHYSICAL_ITEM)
                .cost(10.0).durationHours(0.0).weightKg(1.0).usefulness(90.0).available(true).build();

        // Item B: Usefulness 100, Cost 80 (Efficiency 100/80 = 1.25)
        ResourceOption itemB = ResourceOption.builder()
                .id("B").name("Low Efficiency").category(ResourceCategory.PHYSICAL_ITEM)
                .cost(80.0).durationHours(0.0).weightKg(1.0).usefulness(100.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(100.0)
                .emergencyReserve(50.0) // Effective budget = 50.0
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(5.0)
                .candidateOptions(List.of(itemB, itemA)) // Insert in reverse order
                .build();

        AllocationResult result = greedyService.allocate(problem);

        assertTrue(result.isFeasible());
        assertEquals(1, result.getSelectedResources().size());
        assertEquals("A", result.getSelectedResources().get(0).getId()); // Item A chosen due to higher efficiency ratio
    }

    @Test
    @DisplayName("2. Budget Constraint - Should not exceed effective budget after emergency reserve")
    void testBudgetConstraint() {
        ResourceOption expensive = ResourceOption.builder()
                .id("1").name("Helicopter Tour").category(ResourceCategory.ACTIVITY)
                .cost(300.0).durationHours(1.0).weightKg(0.0).usefulness(99.0).available(true).build();

        ResourceOption affordable = ResourceOption.builder()
                .id("2").name("Walking Tour").category(ResourceCategory.ACTIVITY)
                .cost(30.0).durationHours(2.0).weightKg(0.0).usefulness(60.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(150.0)
                .emergencyReserve(50.0) // Effective budget = 100.0
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(10.0)
                .candidateOptions(List.of(expensive, affordable))
                .build();

        AllocationResult result = greedyService.allocate(problem);

        assertTrue(result.isFeasible());
        assertFalse(result.getSelectedResources().contains(expensive));
        assertTrue(result.getSelectedResources().contains(affordable));
        assertTrue(result.getTotalCost() <= 100.0);
    }

    @Test
    @DisplayName("3. Time Constraint - Should respect maximum available travel hours")
    void testTimeConstraint() {
        ResourceOption longActivity = ResourceOption.builder()
                .id("1").name("All Day Hike").category(ResourceCategory.ACTIVITY)
                .cost(40.0).durationHours(12.0).weightKg(1.0).usefulness(90.0).available(true).build();

        ResourceOption shortActivity = ResourceOption.builder()
                .id("2").name("Museum Visit").category(ResourceCategory.ACTIVITY)
                .cost(20.0).durationHours(2.0).weightKg(0.0).usefulness(70.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(500.0)
                .emergencyReserve(100.0)
                .maxAvailableHours(4.0)
                .maxCarryingCapacityKg(10.0)
                .candidateOptions(List.of(longActivity, shortActivity))
                .build();

        AllocationResult result = greedyService.allocate(problem);

        assertTrue(result.isFeasible());
        assertFalse(result.getSelectedResources().contains(longActivity));
        assertTrue(result.getSelectedResources().contains(shortActivity));
        assertTrue(result.getTotalTime() <= 4.0);
    }

    @Test
    @DisplayName("4. Capacity Constraint - Should respect physical weight carrying capacity")
    void testCapacityConstraint() {
        ResourceOption heavyItem = ResourceOption.builder()
                .id("1").name("Camping Tent").category(ResourceCategory.PHYSICAL_ITEM)
                .cost(50.0).durationHours(0.0).weightKg(10.0).usefulness(80.0).available(true).build();

        ResourceOption lightItem = ResourceOption.builder()
                .id("2").name("Power Bank").category(ResourceCategory.PHYSICAL_ITEM)
                .cost(30.0).durationHours(0.0).weightKg(0.5).usefulness(75.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(500.0)
                .emergencyReserve(100.0)
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(2.0)
                .candidateOptions(List.of(heavyItem, lightItem))
                .build();

        AllocationResult result = greedyService.allocate(problem);

        assertTrue(result.isFeasible());
        assertFalse(result.getSelectedResources().contains(heavyItem));
        assertTrue(result.getSelectedResources().contains(lightItem));
        assertTrue(result.getTotalWeight() <= 2.0);
    }

    @Test
    @DisplayName("5. Unavailable Resources - Should ignore resources marked available=false")
    void testUnavailableResources() {
        ResourceOption unavailable = ResourceOption.builder()
                .id("1").name("Sold Out Express").category(ResourceCategory.TRANSPORTATION)
                .cost(10.0).durationHours(1.0).weightKg(0.0).usefulness(100.0).available(false).build();

        ResourceOption available = ResourceOption.builder()
                .id("2").name("Standard Bus").category(ResourceCategory.TRANSPORTATION)
                .cost(15.0).durationHours(2.0).weightKg(0.0).usefulness(60.0).available(true).build();

        AllocationProblem problem = AllocationProblem.builder()
                .totalBudget(200.0)
                .emergencyReserve(50.0)
                .maxAvailableHours(10.0)
                .maxCarryingCapacityKg(10.0)
                .candidateOptions(List.of(unavailable, available))
                .build();

        AllocationResult result = greedyService.allocate(problem);

        assertTrue(result.isFeasible());
        assertFalse(result.getSelectedResources().contains(unavailable));
        assertTrue(result.getSelectedResources().contains(available));
    }
}
