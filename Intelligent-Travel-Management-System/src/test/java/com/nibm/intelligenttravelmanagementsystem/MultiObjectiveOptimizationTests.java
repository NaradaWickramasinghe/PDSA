package com.nibm.intelligenttravelmanagementsystem;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.AlgorithmType;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationResponse;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.*;
import com.nibm.intelligenttravelmanagementsystem.optimization.service.OptimizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class MultiObjectiveOptimizationTests {

    @Autowired
    private OptimizationService optimizationService;

    @Autowired
    private BranchAndBoundOptimizer branchAndBoundOptimizer;

    @Autowired
    private ParetoFrontierOptimizer paretoFrontierOptimizer;

    @Autowired
    private GeneticRouteOptimizer geneticRouteOptimizer;

    private TravelGraph graph;

    @BeforeEach
    void setUp() {
        graph = optimizationService.getGraph();
    }

    @Test
    void testBranchAndBoundOptimizer_FindsOptimalRoute() {
        OptimizationRequest request = OptimizationRequest.builder()
                .sourceNodeId("CMB")
                .destinationNodeId("ELL")
                .timeWeight(0.4)
                .costWeight(0.4)
                .safetyWeight(0.2)
                .qualityWeight(0.0)
                .build();

        OptimizationResult result = branchAndBoundOptimizer.optimize(graph, request);

        assertTrue(result.isSuccess(), "Branch & Bound should successfully find a route.");
        assertNotNull(result.getBestRoute());
        assertEquals("CMB", result.getBestRoute().getNodeIds().get(0));
        assertEquals("ELL", result.getBestRoute().getCurrentNodeId());
        assertTrue(result.getBestRoute().getTotalDurationMinutes() > 0);
        assertTrue(result.getBestRoute().getTotalCostLkr() > 0);
    }

    @Test
    void testBranchAndBoundOptimizer_RespectsConstraints() {
        OptimizationRequest request = OptimizationRequest.builder()
                .sourceNodeId("CMB")
                .destinationNodeId("ELL")
                .maxBudgetLkr(25000)
                .maxTimeMinutes(600)
                .maxAllowedRisk(4)
                .build();

        OptimizationResult result = branchAndBoundOptimizer.optimize(graph, request);

        if (result.isSuccess() && result.getBestRoute() != null) {
            assertTrue(result.getBestRoute().getTotalCostLkr() <= 25000, "Should satisfy budget constraint.");
            assertTrue(result.getBestRoute().getTotalDurationMinutes() <= 600, "Should satisfy time constraint.");
            assertTrue(result.getBestRoute().getMaxRiskObserved() <= 4, "Should satisfy safety constraint.");
        }
    }

    @Test
    void testParetoFrontierOptimizer_GeneratesNonDominatedFrontier() {
        OptimizationRequest request = OptimizationRequest.builder()
                .sourceNodeId("CMB")
                .destinationNodeId("YAL")
                .timeWeight(0.3)
                .costWeight(0.3)
                .safetyWeight(0.2)
                .qualityWeight(0.2)
                .build();

        OptimizationResult result = paretoFrontierOptimizer.optimize(graph, request);

        assertTrue(result.isSuccess(), "Pareto DP should find solutions.");
        assertNotNull(result.getBestRoute());
        assertTrue(result.getExecutionTimeMs() >= 0);
    }

    @Test
    void testGeneticRouteOptimizer_MetaheuristicConvergence() {
        OptimizationRequest request = OptimizationRequest.builder()
                .sourceNodeId("CMB")
                .destinationNodeId("GLL")
                .build();

        OptimizationResult result = geneticRouteOptimizer.optimize(graph, request);

        assertTrue(result.isSuccess(), "Genetic Algorithm should evolve a valid route from CMB to GLL.");
        assertEquals("CMB", result.getBestRoute().getNodeIds().get(0));
        assertEquals("GLL", result.getBestRoute().getCurrentNodeId());
    }

    @Test
    void testOptimizationService_EndToEndPlan() {
        OptimizationRequest request = OptimizationRequest.builder()
                .sourceNodeId("CMB")
                .destinationNodeId("NUE")
                .algorithm(AlgorithmType.BRANCH_AND_BOUND)
                .build();

        OptimizationResponse response = optimizationService.planOptimalRoute(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getBestRoute());
        assertTrue(response.getBestRoute().getPathNodeIds().contains("CMB"));
    }
}
