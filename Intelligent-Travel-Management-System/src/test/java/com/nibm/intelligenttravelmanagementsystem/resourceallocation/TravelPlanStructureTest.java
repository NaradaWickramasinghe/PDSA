package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.SelectedResourceResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.ResourceAllocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TravelPlanStructureTest {

    @Autowired
    private ResourceAllocationService service;

    @Test
    @DisplayName("1. Valid Travel Plan Scenario - Generates complete plan covering Transportation, Accommodation, Activity, and Equipment")
    void testValidTravelPlanRoleCoverage() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("DYNAMIC_PROGRAMMING")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertTrue(response.isFeasible(), "Valid request should produce a feasible plan.");
        assertFalse(response.getSelectedResources().isEmpty());

        Set<String> categories = response.getSelectedResources().stream()
                .map(SelectedResourceResponse::getCategory)
                .collect(Collectors.toSet());

        // A complete travel plan should include Transportation, Accommodation, Activity, and Equipment
        assertTrue(categories.contains("TRANSPORTATION"), "Plan should contain a transportation option.");
        assertTrue(categories.contains("ACCOMMODATION"), "Plan should contain an accommodation option.");
        assertTrue(categories.contains("ACTIVITY"), "Plan should contain activity options.");
        assertTrue(categories.contains("PHYSICAL_ITEM"), "Plan should contain physical equipment items.");
    }

    @Test
    @DisplayName("2. Invalid Scenario - Zero effective budget should result in infeasible plan")
    void testInfeasibleZeroBudget() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(500.0)
                .emergencyReserve(500.0) // effective budget = 0
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertFalse(response.isFeasible(), "Zero effective budget should result in an infeasible allocation.");
    }

    @Test
    @DisplayName("3. Invalid Scenario - Insufficient time hours should result in infeasible plan")
    void testInfeasibleInsufficientTime() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(0.1) // 6 minutes available
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertNotNull(response);
        assertFalse(response.isFeasible(), "Insufficient available travel hours should result in an infeasible plan.");
    }

    @Test
    @DisplayName("4. Joint Constraint Verification - All selected resources together must satisfy budget, time, and weight constraints")
    void testJointConstraintsSatisfied() {
        ResourceAllocationRequest request = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationResponse response = service.allocateResources(request);

        assertTrue(response.isFeasible());
        List<SelectedResourceResponse> selected = response.getSelectedResources();

        double totalCost = selected.stream().mapToDouble(SelectedResourceResponse::getCost).sum();
        double totalTime = selected.stream().mapToDouble(SelectedResourceResponse::getDurationHours).sum();
        double totalWeight = selected.stream().mapToDouble(SelectedResourceResponse::getWeightKg).sum();

        assertTrue(totalCost <= 45000.0 + 1e-6, "Total cost must not exceed effective budget (45,000 LKR).");
        assertTrue(totalTime <= 16.0 + 1e-6, "Total travel time must not exceed available hours (16.0 h).");
        assertTrue(totalWeight <= 15.0 + 1e-6, "Total luggage weight must not exceed capacity (15.0 kg).");
    }
}
