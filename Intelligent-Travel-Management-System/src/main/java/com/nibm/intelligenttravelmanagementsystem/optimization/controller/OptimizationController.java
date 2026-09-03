package com.nibm.intelligenttravelmanagementsystem.optimization.controller;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.*;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.*;
import com.nibm.intelligenttravelmanagementsystem.optimization.service.BenchmarkService;
import com.nibm.intelligenttravelmanagementsystem.optimization.service.OptimizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/optimization")
public class OptimizationController {

    private final OptimizationService optimizationService;
    private final BenchmarkService benchmarkService;

    public OptimizationController(OptimizationService optimizationService, BenchmarkService benchmarkService) {
        this.optimizationService = optimizationService;
        this.benchmarkService = benchmarkService;
    }

    @GetMapping("/network")
    public ResponseEntity<Map<String, Object>> getNetworkTopology() {
        Map<String, Object> response = new HashMap<>();
        Collection<TravelNode> nodes = optimizationService.getGraph().getAllNodes();
        List<TravelEdge> edges = optimizationService.getGraph().getAllEdges();

        response.put("nodeCount", nodes.size());
        response.put("edgeCount", edges.size());
        response.put("nodes", nodes);
        response.put("edges", edges);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/plan")
    public ResponseEntity<OptimizationResponse> planRoute(@RequestBody OptimizationRequest request) {
        OptimizationResponse response = optimizationService.planOptimalRoute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/benchmark")
    public ResponseEntity<BenchmarkResponse> runBenchmark(
            @RequestParam(required = false, defaultValue = "CMB") String sourceNodeId,
            @RequestParam(required = false, defaultValue = "ELL") String destinationNodeId) {
        BenchmarkResponse response = benchmarkService.runActiveNetworkBenchmark(sourceNodeId, destinationNodeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scalability-suite")
    public ResponseEntity<List<BenchmarkResponse>> getScalabilityBenchmarks() {
        List<BenchmarkResponse> results = benchmarkService.runScalabilityBenchmarkSuite();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/network/refresh")
    public ResponseEntity<Map<String, Object>> refreshNetwork() {
        optimizationService.refreshGraph();
        return getNetworkTopology();
    }
}
