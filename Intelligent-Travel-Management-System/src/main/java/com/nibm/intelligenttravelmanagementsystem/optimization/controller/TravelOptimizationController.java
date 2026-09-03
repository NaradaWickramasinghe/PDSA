package com.nibm.intelligenttravelmanagementsystem.optimization.controller;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.BenchmarkSummary;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationResponse;
import com.nibm.intelligenttravelmanagementsystem.optimization.service.TravelOptimizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/optimization")
public class TravelOptimizationController {
    private final TravelOptimizationService travelOptimizationService;

    public TravelOptimizationController(TravelOptimizationService travelOptimizationService) {
        this.travelOptimizationService = travelOptimizationService;
    }

    @PostMapping("/route")
    public ResponseEntity<OptimizationResponse> optimize(@RequestBody OptimizationRequest request) {
        try {
            return ResponseEntity.ok(travelOptimizationService.optimize(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new OptimizationResponse(
                    request != null ? request.getOptimizationMethod() : "UNKNOWN",
                    java.util.List.of(), 0.0, 0.0, 0.0, 0.0,
                    0L, 0, 0, false, ex.getMessage()));
        }
    }

    @PostMapping("/benchmark")
    public ResponseEntity<BenchmarkSummary> benchmark(@RequestBody OptimizationRequest request) {
        try {
            return ResponseEntity.ok(travelOptimizationService.benchmark(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Optimization service is running.");
    }
}
