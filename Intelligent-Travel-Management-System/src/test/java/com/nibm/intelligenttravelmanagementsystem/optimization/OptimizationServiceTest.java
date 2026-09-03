package com.nibm.intelligenttravelmanagementsystem.optimization;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationResponse;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.ObjectiveWeights;
import com.nibm.intelligenttravelmanagementsystem.optimization.service.TravelOptimizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OptimizationServiceTest {

    @Autowired
    private TravelOptimizationService travelOptimizationService;

    @Test
    void shouldFindFeasibleRouteWithBranchAndBound() {
        OptimizationRequest request = new OptimizationRequest(
                "A",
                "F",
                220.0,
                140.0,
                "BRANCH_AND_BOUND",
                new ObjectiveWeights(0.4, 0.4, 0.2));

        OptimizationResponse response = travelOptimizationService.optimize(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getRoute());
        assertFalse(response.getRoute().isEmpty());
        assertEquals("BRANCH_AND_BOUND", response.getSelectedAlgorithm());
        assertTrue(response.getTotalTravelTime() <= 220.0);
        assertTrue(response.getTotalCost() <= 140.0);
    }

    @Test
    void shouldReturnComparisonAcrossAllAlgorithms() {
        OptimizationRequest request = new OptimizationRequest(
                "A",
                "F",
                260.0,
                200.0,
                "BENCHMARK",
                new ObjectiveWeights(0.5, 0.3, 0.2));

        var comparison = travelOptimizationService.benchmark(request);

        assertNotNull(comparison);
        assertEquals(3, comparison.getResults().size());
        assertTrue(comparison.getResults().stream()
                .allMatch(result -> result.isSuccess() || result.getErrorMessage() != null));
    }
}
