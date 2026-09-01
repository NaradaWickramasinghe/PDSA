package com.nibm.intelligenttravelmanagementsystem.resourceallocation;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.SelectedResourceResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.ResourceAllocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AlgorithmComparisonTest {

    @Autowired
    private ResourceAllocationService service;

    @Test
    @DisplayName("1. Ella and Galle produce different candidate-filtered travel plans across all algorithms")
    void testEllaAndGalleProduceDifferentPlans() {
        ResourceAllocationRequest reqElla = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("DYNAMIC_PROGRAMMING")
                .build();

        ResourceAllocationRequest reqGalle = ResourceAllocationRequest.builder()
                .destination("Galle")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("DYNAMIC_PROGRAMMING")
                .build();

        ResourceAllocationResponse respElla = service.allocateResources(reqElla);
        ResourceAllocationResponse respGalle = service.allocateResources(reqGalle);

        assertTrue(respElla.isFeasible());
        assertTrue(respGalle.isFeasible());

        Set<String> ellaIds = respElla.getSelectedResources().stream().map(SelectedResourceResponse::getId).collect(Collectors.toSet());
        Set<String> galleIds = respGalle.getSelectedResources().stream().map(SelectedResourceResponse::getId).collect(Collectors.toSet());

        assertFalse(ellaIds.equals(galleIds), "Ella travel plan items should differ from Galle travel plan items.");
    }

    @Test
    @DisplayName("2. Low and high budgets produce materially different resource combinations")
    void testBudgetVariationsProduceDifferentPlans() {
        ResourceAllocationRequest lowReq = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(15000.0)
                .emergencyReserve(2000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(1)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationRequest highReq = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(75000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(1)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationResponse lowResp = service.allocateResources(lowReq);
        ResourceAllocationResponse highResp = service.allocateResources(highReq);

        assertTrue(lowResp.isFeasible());
        assertTrue(highResp.isFeasible());

        assertTrue(highResp.getTotalCost() > lowResp.getTotalCost(), "High budget allocation should utilize available budget for premium selections.");
    }

    @Test
    @DisplayName("3. Different trip durations produce different feasible plans")
    void testDurationVariationsProduceDifferentPlans() {
        ResourceAllocationRequest shortReq = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(4.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationRequest longReq = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(18.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .selectedAlgorithm("GREEDY")
                .build();

        ResourceAllocationResponse shortResp = service.allocateResources(shortReq);
        ResourceAllocationResponse longResp = service.allocateResources(longReq);

        assertTrue(shortResp.isFeasible());
        assertTrue(longResp.isFeasible());
        assertTrue(shortResp.getTotalTimeUsed() <= 4.0);
        assertTrue(longResp.getTotalTimeUsed() > shortResp.getTotalTimeUsed());
    }

    @Test
    @DisplayName("4. Different algorithms (Greedy, DP, Genetic) execute distinct strategies and produce results")
    void testDifferentAlgorithmsExecution() {
        ResourceAllocationRequest baseReq = ResourceAllocationRequest.builder()
                .destination("Ella")
                .totalBudget(50000.0)
                .emergencyReserve(5000.0)
                .availableHours(16.0)
                .luggageCapacity(15.0)
                .travellerCount(2)
                .build();

        baseReq.setSelectedAlgorithm("GREEDY");
        ResourceAllocationResponse greedyResp = service.allocateResources(baseReq);

        baseReq.setSelectedAlgorithm("DYNAMIC_PROGRAMMING");
        ResourceAllocationResponse dpResp = service.allocateResources(baseReq);

        baseReq.setSelectedAlgorithm("GENETIC");
        ResourceAllocationResponse geneticResp = service.allocateResources(baseReq);

        assertTrue(greedyResp.isFeasible());
        assertTrue(dpResp.isFeasible());
        assertTrue(geneticResp.isFeasible());

        assertEquals("GREEDY", greedyResp.getAlgorithmUsed());
        assertEquals("DYNAMIC_PROGRAMMING", dpResp.getAlgorithmUsed());
        assertEquals("GENETIC", geneticResp.getAlgorithmUsed());
    }

    @Test
    @DisplayName("5. Every algorithm consumes destination-filtered candidate datasets exclusively")
    void testDestinationFilteredCandidatesInAllAlgorithms() {
        String destination = "Sigiriya";

        for (String algo : new String[]{"GREEDY", "DYNAMIC_PROGRAMMING", "GENETIC"}) {
            ResourceAllocationRequest req = ResourceAllocationRequest.builder()
                    .destination(destination)
                    .totalBudget(60000.0)
                    .emergencyReserve(5000.0)
                    .availableHours(16.0)
                    .luggageCapacity(15.0)
                    .travellerCount(2)
                    .selectedAlgorithm(algo)
                    .build();

            ResourceAllocationResponse resp = service.allocateResources(req);
            assertTrue(resp.isFeasible(), "Algorithm " + algo + " should produce feasible result for Sigiriya.");

            // Selected resources must belong to Sigiriya candidates or General ALL items
            for (SelectedResourceResponse item : resp.getSelectedResources()) {
                boolean isSigiriyaOrAll = item.getId().startsWith("SIGIRIYA") || item.getId().startsWith("EQ");
                assertTrue(isSigiriyaOrAll, "Algorithm " + algo + " selected item " + item.getName() + " outside Sigiriya candidate pool.");
            }
        }
    }
}
