package com.nibm.intelligenttravelmanagementsystem.optimization.service;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.BenchmarkResponse;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BenchmarkService {

    private final BranchAndBoundOptimizer branchAndBoundOptimizer;
    private final ParetoFrontierOptimizer paretoFrontierOptimizer;
    private final GeneticRouteOptimizer geneticRouteOptimizer;
    private final KnapsackOptimizer knapsackOptimizer;
    private final ModuleIntegrationService moduleIntegrationService;
    private final OptimizationService optimizationService;

    public BenchmarkService(BranchAndBoundOptimizer branchAndBoundOptimizer,
                            ParetoFrontierOptimizer paretoFrontierOptimizer,
                            GeneticRouteOptimizer geneticRouteOptimizer,
                            KnapsackOptimizer knapsackOptimizer,
                            ModuleIntegrationService moduleIntegrationService,
                            OptimizationService optimizationService) {
        this.branchAndBoundOptimizer = branchAndBoundOptimizer;
        this.paretoFrontierOptimizer = paretoFrontierOptimizer;
        this.geneticRouteOptimizer = geneticRouteOptimizer;
        this.knapsackOptimizer = knapsackOptimizer;
        this.moduleIntegrationService = moduleIntegrationService;
        this.optimizationService = optimizationService;
    }

    public BenchmarkResponse runActiveNetworkBenchmark(String sourceId, String destId) {
        TravelGraph graph = optimizationService.getGraph();

        if (sourceId == null || !graph.hasNode(sourceId)) sourceId = "CMB";
        if (destId == null || !graph.hasNode(destId)) destId = "ELL";

        OptimizationRequest request = OptimizationRequest.builder()
                .sourceNodeId(sourceId)
                .destinationNodeId(destId)
                .timeWeight(0.35)
                .costWeight(0.35)
                .safetyWeight(0.20)
                .qualityWeight(0.10)
                .build();

        return evaluateAlgorithmsOnGraph("Active Travel Network Benchmark", graph, request);
    }

    public List<BenchmarkResponse> runScalabilityBenchmarkSuite() {
        List<BenchmarkResponse> suiteResults = new ArrayList<>();

        TravelGraph smallGraph = generateSyntheticNetwork(10, 25);
        suiteResults.add(evaluateAlgorithmsOnGraph("Small Synthetic Graph (N=10, E=25)", smallGraph,
                OptimizationRequest.builder().sourceNodeId("N_0").destinationNodeId("N_9").build()));

        TravelGraph mediumGraph = generateSyntheticNetwork(50, 160);
        suiteResults.add(evaluateAlgorithmsOnGraph("Medium Synthetic Graph (N=50, E=160)", mediumGraph,
                OptimizationRequest.builder().sourceNodeId("N_0").destinationNodeId("N_49").build()));

        TravelGraph largeGraph = generateSyntheticNetwork(150, 600);
        suiteResults.add(evaluateAlgorithmsOnGraph("Large Synthetic Graph (N=150, E=600)", largeGraph,
                OptimizationRequest.builder().sourceNodeId("N_0").destinationNodeId("N_149").build()));

        return suiteResults;
    }

    private BenchmarkResponse evaluateAlgorithmsOnGraph(String scenarioName, TravelGraph graph, OptimizationRequest req) {
        Map<String, BenchmarkResponse.AlgorithmMetricDTO> metricsMap = new LinkedHashMap<>();

        List<OptimizationAlgorithm> algorithms = List.of(
                branchAndBoundOptimizer,
                paretoFrontierOptimizer,
                geneticRouteOptimizer
        );

        for (OptimizationAlgorithm algo : algorithms) {
            algo.optimize(graph, req);

            int iterations = 5;
            double totalTime = 0.0;
            double totalMem = 0.0;
            OptimizationResult lastResult = null;

            for (int i = 0; i < iterations; i++) {
                OptimizationResult res = algo.optimize(graph, req);
                totalTime += res.getExecutionTimeMs();
                totalMem += res.getMemoryUsedKb();
                lastResult = res;
            }

            double avgTime = Math.round((totalTime / iterations) * 100.0) / 100.0;
            double avgMem = Math.round((totalMem / iterations) * 100.0) / 100.0;

            BenchmarkResponse.AlgorithmMetricDTO dto;
            if (lastResult != null && lastResult.getBestRoute() != null) {
                dto = BenchmarkResponse.AlgorithmMetricDTO.builder()
                        .algorithmName(algo.getName())
                        .executionTimeMs(avgTime)
                        .memoryUsedKb(avgMem)
                        .bestCompositeScore(Math.round(lastResult.getBestRoute().getCompositeScore() * 1000.0) / 1000.0)
                        .totalDistanceKm(Math.round(lastResult.getBestRoute().getTotalDistanceKm() * 10.0) / 10.0)
                        .totalDurationMinutes(Math.round(lastResult.getBestRoute().getTotalDurationMinutes() * 10.0) / 10.0)
                        .totalCostLkr(lastResult.getBestRoute().getTotalCostLkr())
                        .averageRiskLevel(Math.round(lastResult.getBestRoute().getAverageRiskLevel() * 100.0) / 100.0)
                        .nodesExplored(lastResult.getNodesExplored())
                        .foundValidPath(true)
                        .build();
            } else {
                dto = BenchmarkResponse.AlgorithmMetricDTO.builder()
                        .algorithmName(algo.getName())
                        .executionTimeMs(avgTime)
                        .memoryUsedKb(avgMem)
                        .foundValidPath(false)
                        .build();
            }

            metricsMap.put(algo.getName(), dto);
        }

        // Also evaluate Knapsack Dynamic Programming on integrated candidate set
        if (knapsackOptimizer != null && moduleIntegrationService != null) {
            List<IntegratedCandidate> candidates = moduleIntegrationService.collectIntegratedCandidates(req, graph);
            KnapsackOptimizer.KnapsackResult knapRes = knapsackOptimizer.solve(candidates, req);
            BenchmarkResponse.AlgorithmMetricDTO knapDTO = BenchmarkResponse.AlgorithmMetricDTO.builder()
                    .algorithmName(knapsackOptimizer.getName())
                    .executionTimeMs(knapRes.getExecutionTimeMs())
                    .memoryUsedKb(knapRes.getMemoryUsedKb())
                    .bestCompositeScore(knapRes.getTotalUtility())
                    .totalDurationMinutes(knapRes.getTotalDurationMinutes())
                    .totalCostLkr((int) knapRes.getTotalCost())
                    .nodesExplored(knapRes.getStatesEvaluated())
                    .foundValidPath(!knapRes.getSelectedCandidates().isEmpty())
                    .build();
            metricsMap.put(knapsackOptimizer.getName(), knapDTO);
        }

        return BenchmarkResponse.builder()
                .scenarioName(scenarioName)
                .networkNodesCount(graph.getNodeCount())
                .networkEdgesCount(graph.getEdgeCount())
                .algorithmMetrics(metricsMap)
                .build();
    }

    private TravelGraph generateSyntheticNetwork(int nodeCount, int edgeCount) {
        TravelGraph graph = new TravelGraph();
        Random rng = new Random(42);

        for (int i = 0; i < nodeCount; i++) {
            graph.addNode(TravelNode.builder()
                    .nodeId("N_" + i)
                    .name("Waypoint " + i)
                    .nodeType("SYNTHETIC")
                    .province("Province_" + (i % 5))
                    .latitude(6.0 + rng.nextDouble() * 3.5)
                    .longitude(79.5 + rng.nextDouble() * 2.5)
                    .build());
        }

        for (int i = 0; i < nodeCount - 1; i++) {
            addSyntheticEdge(graph, "E_SPINE_" + i, "N_" + i, "N_" + (i + 1), rng);
        }

        int remainingEdges = edgeCount - (nodeCount - 1);
        for (int e = 0; e < remainingEdges; e++) {
            int u = rng.nextInt(nodeCount);
            int v = rng.nextInt(nodeCount);
            if (u != v) {
                addSyntheticEdge(graph, "E_RAND_" + e, "N_" + u, "N_" + v, rng);
            }
        }

        return graph;
    }

    private void addSyntheticEdge(TravelGraph g, String id, String u, String v, Random rng) {
        double dist = Math.round((10.0 + rng.nextDouble() * 120.0) * 10.0) / 10.0;
        int time = (int) (dist * (1.2 + rng.nextDouble() * 0.8));
        int cost = (int) (dist * 40.0 + rng.nextInt(500));
        int qual = 1 + rng.nextInt(5);
        int traf = 1 + rng.nextInt(4);
        int acc = 1 + rng.nextInt(5);
        int risk = 1 + rng.nextInt(3);

        g.addEdge(TravelEdge.builder()
                .edgeId(id + "_F")
                .source(u).destination(v)
                .distanceKm(dist).travelTimeMinutes(time).estimatedCostLkr(cost)
                .roadQuality(qual).trafficLevel(traf).transportMode("ROAD")
                .accessibility(acc).riskLevel(risk)
                .build());

        g.addEdge(TravelEdge.builder()
                .edgeId(id + "_R")
                .source(v).destination(u)
                .distanceKm(dist).travelTimeMinutes(time).estimatedCostLkr(cost)
                .roadQuality(qual).trafficLevel(traf).transportMode("ROAD")
                .accessibility(acc).riskLevel(risk)
                .build());
    }
}
