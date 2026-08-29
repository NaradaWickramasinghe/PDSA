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
    private final OptimizationService optimizationService;

    public BenchmarkService(BranchAndBoundOptimizer branchAndBoundOptimizer,
                            ParetoFrontierOptimizer paretoFrontierOptimizer,
                            GeneticRouteOptimizer geneticRouteOptimizer,
                            OptimizationService optimizationService) {
        this.branchAndBoundOptimizer = branchAndBoundOptimizer;
        this.paretoFrontierOptimizer = paretoFrontierOptimizer;
        this.geneticRouteOptimizer = geneticRouteOptimizer;
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

        return BenchmarkResponse.builder()
                .scenarioName(scenarioName)
                .networkNodesCount(graph.getNodeCount())
                .networkEdgesCount(graph.getEdgeCount())
                .sourceNodeId(req.getSourceNodeId())
                .destinationNodeId(req.getDestinationNodeId())
                .algorithmMetrics(metricsMap)
                .build();
    }

    private TravelGraph generateSyntheticNetwork(int numNodes, int numEdges) {
        TravelGraph graph = new TravelGraph();
        Random rng = new Random(100);

        for (int i = 0; i < numNodes; i++) {
            graph.addNode(TravelNode.builder()
                    .nodeId("N_" + i)
                    .name("Station " + i)
                    .latitude(6.0 + rng.nextDouble() * 3.0)
                    .longitude(79.5 + rng.nextDouble() * 2.0)
                    .build());
        }

        for (int i = 0; i < numNodes - 1; i++) {
            addEdge(graph, "N_" + i, "N_" + (i + 1), rng);
        }

        for (int i = 0; i < numEdges - (numNodes - 1); i++) {
            int u = rng.nextInt(numNodes);
            int v = rng.nextInt(numNodes);
            if (u != v) {
                addEdge(graph, "N_" + u, "N_" + v, rng);
            }
        }

        return graph;
    }

    private void addEdge(TravelGraph g, String u, String v, Random rng) {
        double dist = 10.0 + rng.nextDouble() * 50.0;
        int time = (int) (dist * (0.8 + rng.nextDouble() * 0.8));
        int cost = (int) (dist * (15.0 + rng.nextDouble() * 20.0));
        int quality = 1 + rng.nextInt(5);
        int traffic = 1 + rng.nextInt(5);
        int risk = 1 + rng.nextInt(4);

        g.addEdge(TravelEdge.builder()
                .edgeId("E_" + u + "_" + v + "_" + rng.nextInt(1000))
                .source(u)
                .destination(v)
                .distanceKm(dist)
                .travelTimeMinutes(time)
                .estimatedCostLkr(cost)
                .roadQuality(quality)
                .trafficLevel(traffic)
                .transportMode("ROAD")
                .accessibility(3)
                .riskLevel(risk)
                .build());
    }
}
