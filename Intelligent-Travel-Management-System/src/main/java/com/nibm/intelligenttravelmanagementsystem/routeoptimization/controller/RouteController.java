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

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
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
                    request.isPrioritizeTime()
            );
        } else {
            // Single route - without risk
            result = routeService.findRoute(
                    request.getStartLocationId(),
                    request.getEndLocationId(),
                    request.getTransportMode(),
                    request.isPrioritizeTime()
            );
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/multi-stop")
    public ResponseEntity<RouteResult> findMultiStopRoute(@RequestBody MultiStopRequest request) {
        // Without risk
        RouteResult result = routeService.findMultiStopRoute(
                request.getStartLocationId(),      // Start point
                request.getDestinationIds(),
                request.getTransportMode(),
                request.isPrioritizeTime()
        );
        return ResponseEntity.ok(result);
    }
}