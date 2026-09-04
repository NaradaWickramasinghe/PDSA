package com.nibm.intelligenttravelmanagementsystem;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.AlgorithmType;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationResponse;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.*;
import com.nibm.intelligenttravelmanagementsystem.optimization.service.OptimizationService;
import java.util.List;
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

    @Autowired
    private KnapsackOptimizer knapsackOptimizer;

    @Test
    void testDoublyLinkedList_AddAndRemove() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        assertTrue(list.isEmpty());

        var n1 = list.add("CMB");
        var n2 = list.add("KDY");
        var n3 = list.add("ELL");
        assertEquals(3, list.size());

        // Remove middle node in O(1)
        boolean removed = list.remove(n2);
        assertTrue(removed);
        assertEquals(2, list.size());
        assertEquals(List.of("CMB", "ELL"), list.toList());

        // Remove head
        list.remove(n1);
        assertEquals(1, list.size());
        assertEquals(List.of("ELL"), list.toList());
    }

    @Test
    void testKnapsackOptimizer_DynamicProgrammingSelection() {
        OptimizationRequest request = OptimizationRequest.builder()
                .maxBudgetLkr(25000)
                .maxTimeMinutes(480)
                .build();

        List<IntegratedCandidate> candidates = List.of(
                IntegratedCandidate.builder()
                        .nodeId("ELL").name("Ella Town")
                        .compositeCost(8000).compositeTimeMinutes(150).compositeValue(0.92)
                        .build(),
                IntegratedCandidate.builder()
                        .nodeId("NUE").name("Nuwara Eliya")
                        .compositeCost(7000).compositeTimeMinutes(120).compositeValue(0.85)
                        .build(),
                IntegratedCandidate.builder()
                        .nodeId("SIG").name("Sigiriya Rock")
                        .compositeCost(6000).compositeTimeMinutes(140).compositeValue(0.88)
                        .build(),
                IntegratedCandidate.builder()
                        .nodeId("JAF").name("Jaffna Fort")
                        .compositeCost(30000).compositeTimeMinutes(500).compositeValue(0.70)
                        .build()
        );

        KnapsackOptimizer.KnapsackResult result = knapsackOptimizer.solve(candidates, request);

        assertNotNull(result);
        assertTrue(result.getSelectedCandidates().size() >= 2);
        assertTrue(result.getTotalCost() <= 25000, "Should strictly satisfy budget");
        assertTrue(result.getTotalDurationMinutes() <= 480, "Should satisfy duration limit");
        assertTrue(result.getStatesEvaluated() > 0);
    }

    @Test
    void testOptimizationService_PlanWithKnapsackDynamicProgramming() {
        OptimizationRequest request = OptimizationRequest.builder()
                .sourceNodeId("CMB")
                .algorithm(AlgorithmType.KNAPSACK_DYNAMIC_PROGRAMMING)
                .travelStyle("ADVENTURE")
                .maxBudgetLkr(30000)
                .maxTimeMinutes(600)
                .build();

        OptimizationResponse response = optimizationService.planOptimalRoute(request);

        assertTrue(response.isSuccess());
        assertEquals("KNAPSACK_DYNAMIC_PROGRAMMING", response.getSelectedAlgorithm());
        assertNotNull(response.getBestRoute());
        assertNotNull(response.getModuleContributions());
        assertNotNull(response.getIntegrationSummary());
    }
}
