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
            @PathVariable Long edgeId,
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

    @GetMapping("/locations/traffic/{id}")
    public ResponseEntity<Map<String, Object>> getLocationTraffic(@PathVariable Long id) {
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
}

