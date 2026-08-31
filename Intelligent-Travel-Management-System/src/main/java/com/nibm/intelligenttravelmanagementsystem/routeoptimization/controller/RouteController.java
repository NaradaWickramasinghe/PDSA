package com.nibm.intelligenttravelmanagementsystem.routeoptimization.controller;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto.MultiStopRequest;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto.RouteRequest;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto.RouteResult;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.Location;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.service.GraphService;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.service.RouteOptimizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(originPatterns = "http://localhost:*")
@RequiredArgsConstructor
public class RouteController {

    private final RouteOptimizationService routeService;
    private final GraphService graphService;

    @GetMapping("/locations")
    public ResponseEntity<List<Location>> getAllLocations() {
        try {
            List<Location> locations = graphService.getAllLocations();
            System.out.println("📍 Returning " + locations.size() + " locations");
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            System.err.println("Error getting locations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/find")
    public ResponseEntity<RouteResult> findRoute(@RequestBody RouteRequest request) {
        RouteResult result;

        if (request.getMultipleLocations() != null && !request.getMultipleLocations().isEmpty()) {
            // Multi-stop - without risk
            result = routeService.findMultiStopRoute(
                    request.getStartLocationId(),  // Start point
                    request.getMultipleLocations(),
                    request.getTransportMode(),
                    request.isPrioritizeTime(),
                    request.isPreferSafeRoute(),
                    request.getMaxRiskLevel()
            );
        } else {
            // Single route - without risk
            result = routeService.findRoute(
                    request.getStartLocationId(),
                    request.getEndLocationId(),
                    request.getTransportMode(),
                    request.isPrioritizeTime(),
                    request.isPreferSafeRoute(),
                    request.getMaxRiskLevel()
            );
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/multi-stop")
    public ResponseEntity<RouteResult> findMultiStopRoute(@RequestBody MultiStopRequest request) {
        RouteResult result = routeService.findMultiStopRoute(
                request.getStartLocationId(),      // Start point
                request.getDestinationIds(),
                request.getTransportMode(),
                request.isPrioritizeTime(),
                request.isPreferSafeRoute(),
                request.getMaxRiskLevel()
                );
        return ResponseEntity.ok(result);
    }
    @GetMapping("/debug/nodes")
    public ResponseEntity<Map<String, Object>> debugNodes() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Location> locations = graphService.getAllLocations();
            response.put("totalLocations", locations.size());
            response.put("sampleIds", locations.stream()
                    .limit(10)
                    .map(loc -> Map.of(
                            "id", loc.getId(),
                            "name", loc.getName(),
                            "type", loc.getType()
                    ))
                    .collect(Collectors.toList()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/locations/search")
    public ResponseEntity<List<Location>> searchLocations(@RequestParam String query) {
        try {
            List<Location> results = graphService.searchLocations(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}