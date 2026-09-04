package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.SelectedResourceResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.JsonResourceDataProvider;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.ResourceAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DestinationAwareAllocationTest {

    @Autowired
    private ResourceAllocationService service;

    private JsonResourceDataProvider jsonDataProvider;

    @BeforeEach
    void setUp() {
        jsonDataProvider = new JsonResourceDataProvider(new ObjectMapper());
    }

    @Test
    @DisplayName("1. Different destinations produce different candidate resource pools")
    void testDifferentDestinationsCandidatePools() {
        List<ResourceOption> ellaCandidates = jsonDataProvider.getCandidateOptions("Ella");
        List<ResourceOption> galleCandidates = jsonDataProvider.getCandidateOptions("Galle");
        List<ResourceOption> sigiriyaCandidates = jsonDataProvider.getCandidateOptions("Sigiriya");

        assertFalse(ellaCandidates.isEmpty());
        assertFalse(galleCandidates.isEmpty());
        assertFalse(sigiriyaCandidates.isEmpty());

        Set<String> ellaNames = ellaCandidates.stream().map(ResourceOption::getName).collect(Collectors.toSet());
        Set<String> galleNames = galleCandidates.stream().map(ResourceOption::getName).collect(Collectors.toSet());

        // Pools for different destinations must be distinct
        assertFalse(ellaNames.equals(galleNames), "Ella candidate pool should differ from Galle candidate pool.");
    }

    @Test
    @DisplayName("2. Different destinations produce materially different allocation results")
    void testDifferentDestinationsAllocationResults() {
        ResourceAllocationRequest requestElla = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("DYNAMIC_PROGRAMMING")
                .build();

        ResourceAllocationRequest requestGalle = ResourceAllocationRequest.builder()
                .destination("Galle")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("DYNAMIC_PROGRAMMING")
                .build();

        ResourceAllocationResponse responseElla = service.allocateResources(requestElla);
        ResourceAllocationResponse responseGalle = service.allocateResources(requestGalle);

        assertTrue(responseElla.isFeasible());
        assertTrue(responseGalle.isFeasible());

        Set<String> ellaSelected = responseElla.getSelectedResources().stream()
                .map(SelectedResourceResponse::getName)
                .collect(Collectors.toSet());

        Set<String> galleSelected = responseGalle.getSelectedResources().stream()
                .map(SelectedResourceResponse::getName)
                .collect(Collectors.toSet());

        assertFalse(ellaSelected.equals(galleSelected), "Ella allocation result must differ materially from Galle allocation result.");

        assertTrue(responseElla.getSelectedResources().stream().anyMatch(r -> (r.getId() != null && r.getId().startsWith("ELLA")) || r.getName().contains("Ella")),
                "Ella response should contain Ella destination resources.");

        assertTrue(responseGalle.getSelectedResources().stream().anyMatch(r -> (r.getId() != null && r.getId().startsWith("GALLE")) || r.getName().contains("Galle")),
                "Galle response should contain Galle destination resources.");
    }

    @Test
    @DisplayName("3. Changing budget changes the selected resource combination")
    void testBudgetChangesResourceSelection() {
        ResourceAllocationRequest lowBudgetRequest = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(12000.0)
                .emergencyReserve(2000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(1)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationRequest highBudgetRequest = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(70000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(1)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationResponse lowBudgetResponse = service.allocateResources(lowBudgetRequest);
        ResourceAllocationResponse highBudgetResponse = service.allocateResources(highBudgetRequest);

        assertTrue(lowBudgetResponse.isFeasible());
        assertTrue(highBudgetResponse.isFeasible());

        assertTrue(lowBudgetResponse.getTotalCost() <= 10000.0, "Low budget total cost should be within effective budget.");
        assertTrue(highBudgetResponse.getTotalCost() > lowBudgetResponse.getTotalCost(), "High budget allocation should utilize available budget for richer options.");
        assertNotEquals(lowBudgetResponse.getSelectedResources().size(), highBudgetResponse.getSelectedResources().size(), "Resource selections should differ based on budget.");
    }

    @Test
    @DisplayName("4. Changing trip duration / available travel time affects feasible resources")
    void testDurationAffectsFeasibleResources() {
        ResourceAllocationRequest shortTimeRequest = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(3.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationRequest longTimeRequest = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(18.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationResponse shortResponse = service.allocateResources(shortTimeRequest);
        ResourceAllocationResponse longResponse = service.allocateResources(longTimeRequest);

        assertTrue(shortResponse.isFeasible());
        assertTrue(longResponse.isFeasible());

        assertTrue(shortResponse.getTotalTimeUsed() <= 3.0, "Short duration response must respect 3-hour constraint.");
        assertTrue(longResponse.getTotalTimeUsed() > shortResponse.getTotalTimeUsed(), "Long duration response should utilize additional available travel hours.");
    }

    @Test
    @DisplayName("5. Traveller count affects transportation/accommodation capacity feasibility")
    void testTravellerCountAffectsCapacityFeasibility() {
        // Request for 5 travellers
        ResourceAllocationRequest groupRequest = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(80000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(5)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationResponse groupResponse = service.allocateResources(groupRequest);
        assertTrue(groupResponse.isFeasible());

        // 3-seat Tuk-Tuk should be filtered out for 5 travellers
        boolean containsTukTuk = groupResponse.getSelectedResources().stream()
                .anyMatch(r -> r.getName().toLowerCase().contains("tuk-tuk"));

        assertFalse(containsTukTuk, "A 3-capacity tuk-tuk should be excluded for 5 travellers.");
    }
}
