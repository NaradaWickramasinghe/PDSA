package com.nibm.intelligenttravelmanagementsystem.routeoptimization.controller;

import com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto.RouteResult;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.Location;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.service.GraphService;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.service.RouteOptimizationService;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto.RouteRequest;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto.MultiStopRequest;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(originPatterns = "http://localhost:*")
@RequiredArgsConstructor
public class RouteController {

    private final RouteOptimizationService routeService;
    private final GraphService graphService;
    private final TrafficService trafficService;

    // ... existing endpoints ...

    @GetMapping("/traffic/status")
    public ResponseEntity<Map<String, Object>> getTrafficStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("peakHour", trafficService.isPeakHour(LocalTime.now()));
        status.put("weekend", trafficService.isWeekend());
        status.put("trafficLevels", "1-5 (1=Low, 5=Severe)");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/traffic/update/{edgeId}")
    public ResponseEntity<String> updateTraffic(
            @PathVariable String edgeId,
            @RequestParam int level) {
        trafficService.updateTraffic(edgeId, level);
        return ResponseEntity.ok("Traffic updated for edge " + edgeId + " to level " + level);
    }

    @GetMapping("/traffic/simulate")
    public ResponseEntity<Map<String, Object>> simulateTraffic() {
        trafficService.applyTrafficSimulation(graphService.getGraph());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Traffic simulation applied");
        response.put("currentTime", java.time.LocalDateTime.now());
        response.put("isPeakHour", trafficService.isPeakHour());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/calculate")
    public ResponseEntity<RouteResult> calculateRoute(@RequestBody RouteRequest request) {
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

    @GetMapping("/locations/traffic/{id}")
    public ResponseEntity<Map<String, Object>> getLocationTraffic(@PathVariable String id) {
        Location loc = graphService.getLocation(id);
        if (loc == null) {
            return ResponseEntity.notFound().build();
        }

        // Get all edges connected to this location
        var graph = graphService.getGraph();
        var edges = graph.get(id);

        Map<String, Object> trafficInfo = new HashMap<>();
        trafficInfo.put("location", loc.getName());
        trafficInfo.put("connectedSegments", edges != null ? edges.size() : 0);

        if (edges != null && !edges.isEmpty()) {
            double avgTraffic = edges.stream()
                    .mapToInt(e -> trafficService.getTrafficLevel(e.getId(), e))
                    .average()
                    .orElse(0.0);
            trafficInfo.put("averageTraffic", String.format("%.1f", avgTraffic));
            trafficInfo.put("status", avgTraffic >= 4 ? "HEAVY" : avgTraffic >= 3 ? "MODERATE" : "LOW");
        }

        return ResponseEntity.ok(trafficInfo);
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
    @GetMapping("/traffic/all")
    public ResponseEntity<List<Map<String, Object>>> getAllTraffic() {
        List<Map<String, Object>> congestedTraffic = graphService.getGraph().values().stream()
                .flatMap(List::stream)
                .map(edge -> {
                    int trafficLevel = trafficService.getTrafficLevel(edge.getId(), edge);
                    if (trafficLevel >= 3) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("edgeId", edge.getId());
                        data.put("trafficLevel", trafficLevel);
                        // Using midpoint for the red dot
                        double midLat = (edge.getSource().getLatitude() + edge.getDestination().getLatitude()) / 2.0;
                        double midLon = (edge.getSource().getLongitude() + edge.getDestination().getLongitude()) / 2.0;
                        data.put("latitude", midLat);
                        data.put("longitude", midLon);
                        data.put("startName", edge.getSource().getName());
                        data.put("endName", edge.getDestination().getName());
                        return data;
                    }
                    return null;
                })
                .filter(data -> data != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(congestedTraffic);
    }
}
