package com.nibm.intelligenttravelmanagementsystem.routeoptimization.service;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;
import com.nibm.intelligenttravelmanagementsystem.shared.db.repositories.NodeRepository;
import com.nibm.intelligenttravelmanagementsystem.shared.db.repositories.EdgeRepository;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.Location;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.RouteEdge;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphService {

    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;

    private Map<String, List<RouteEdge>> graph = new HashMap<>();
    private Map<String, Location> locationMap = new HashMap<>();
    private Map<String, Location> locationNameMap = new HashMap<>();

    @PostConstruct
    public void init() {
        loadGraphFromDatabase();
    }

    public void loadGraphFromDatabase() {
        log.info("🔄 Loading graph from database...");

        graph.clear();
        locationMap.clear();
        locationNameMap.clear();

        // 1. Load all nodes
        List<Node> nodes = nodeRepository.findAll();

        if (nodes.isEmpty()) {
            log.warn("⚠️ No nodes found in database!");
            return;
        }

        log.info("📊 Found {} nodes in database", nodes.size());

        // Convert nodes to locations
        for (Node node : nodes) {
            // ✅ The ID is already a String (varchar)
            String nodeId = node.getNodeId();  // This is the String ID from the database

            log.debug("🔍 Loading node: id={}, name={}, type={}", nodeId, node.getName(), node.getNodeType());

            Location loc = new Location(
                    nodeId,  // Use the String ID
                    node.getName(),
                    node.getLatitude() != null ? node.getLatitude() : 0.0,
                    node.getLongitude() != null ? node.getLongitude() : 0.0,
                    node.getNodeType() != null ? node.getNodeType() : "unknown"
            );

            loc.setProvince(node.getProvince());
            loc.setDistrict(node.getDistrict());
            loc.setDescription(node.getDescription());

            locationMap.put(nodeId, loc);
            locationNameMap.put(node.getName().toLowerCase(), loc);
            graph.put(nodeId, new ArrayList<>());
        }

        log.info("✅ Loaded {} locations", locationMap.size());

        // 2. Load all edges
        List<Edge> sharedEdges = edgeRepository.findAll();

        if (sharedEdges.isEmpty()) {
            log.warn("⚠️ No edges found in database!");
            return;
        }

        log.info("📊 Found {} edges in database", sharedEdges.size());

        int edgeCount = 0;
        int skippedCount = 0;

        for (Edge sharedEdge : sharedEdges) {
            // ✅ Use the source and destination as String IDs
            String sourceId = sharedEdge.getSource();
            String targetId = sharedEdge.getDestination();

            // If source/destination are null, try the numeric fields
            if (sourceId == null && sharedEdge.getSourceNodeId() != null) {
                sourceId = String.valueOf(sharedEdge.getSourceNodeId());
            }
            if (targetId == null && sharedEdge.getTargetNodeId() != null) {
                targetId = String.valueOf(sharedEdge.getTargetNodeId());
            }

            Location source = locationMap.get(sourceId);
            Location target = locationMap.get(targetId);

            if (source == null || target == null) {
                log.debug("⚠️ Skipping edge: source={}, target={} not found", sourceId, targetId);
                skippedCount++;
                continue;
            }

            // Get travel time
            int travelTime = sharedEdge.getTravelTimeMinutes() != null ?
                    sharedEdge.getTravelTimeMinutes() : 30;

            // Get distance
            double distance = sharedEdge.getDistanceKm() != null ?
                    sharedEdge.getDistanceKm() : 10.0;

            // Get risk level
            int riskLevel = sharedEdge.getRiskLevel() != null ?
                    sharedEdge.getRiskLevel().intValue() : 1;

            // ✅ Use edgeId as String ID
            String edgeId = sharedEdge.getEdgeId() != null ?
                    sharedEdge.getEdgeId() : "edge_" + sourceId + "_" + targetId;

            // Create RouteEdge
            RouteEdge routeEdge = new RouteEdge(
                    edgeId,
                    source,
                    target,
                    distance,
                    travelTime,
                    riskLevel,
                    sharedEdge.getTransportMode() != null ? sharedEdge.getTransportMode() : "road",
                    false
            );

            // Add additional fields
            routeEdge.setEstimatedCostLkr(sharedEdge.getEstimatedCostLkr() != null ?
                    sharedEdge.getEstimatedCostLkr().doubleValue() : null);
            routeEdge.setRoadQuality(sharedEdge.getRoadQuality() != null ?
                    sharedEdge.getRoadQuality().intValue() : null);
            routeEdge.setTrafficLevel(sharedEdge.getTrafficLevel() != null ?
                    sharedEdge.getTrafficLevel().intValue() : null);
            routeEdge.setAccesibility(sharedEdge.getAccessibility() != null ?
                    sharedEdge.getAccessibility().intValue() : null);

            // Add to graph
            graph.get(sourceId).add(routeEdge);
            edgeCount++;

            // Add reverse edge
            RouteEdge reverseEdge = new RouteEdge(
                    edgeId + "_rev",
                    target,
                    source,
                    distance,
                    travelTime,
                    riskLevel,
                    sharedEdge.getTransportMode() != null ? sharedEdge.getTransportMode() : "road",
                    false
            );
            reverseEdge.setEstimatedCostLkr(sharedEdge.getEstimatedCostLkr() != null ?
                    sharedEdge.getEstimatedCostLkr().doubleValue() : null);
            reverseEdge.setRoadQuality(sharedEdge.getRoadQuality() != null ?
                    sharedEdge.getRoadQuality().intValue() : null);
            reverseEdge.setTrafficLevel(sharedEdge.getTrafficLevel() != null ?
                    sharedEdge.getTrafficLevel().intValue() : null);
            reverseEdge.setAccesibility(sharedEdge.getAccessibility() != null ?
                    sharedEdge.getAccessibility().intValue() : null);

            graph.get(targetId).add(reverseEdge);
            edgeCount++;
        }

        log.info("✅ Loaded {} edges, {} total connections", sharedEdges.size(), getTotalEdges());
        if (skippedCount > 0) {
            log.warn("⚠️ Skipped {} edges due to missing source/target nodes", skippedCount);
        }
    }

    public Map<String, List<RouteEdge>> getGraph() {
        return graph;
    }

    public Location getLocation(String id) {
        return locationMap.get(id);
    }

    public Location getLocationByName(String name) {
        return locationNameMap.get(name.toLowerCase());
    }

    public List<Location> getAllLocations() {
        return new ArrayList<>(locationMap.values());
    }

    public List<Location> searchLocations(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return getAllLocations();
        }

        String lowerSearch = searchTerm.toLowerCase();
        List<Location> results = new ArrayList<>();

        for (Location loc : locationMap.values()) {
            if (loc.getName().toLowerCase().contains(lowerSearch)) {
                results.add(loc);
            }
        }
        return results;
    }

    public List<String> getProvinces() {
        Set<String> provinces = new HashSet<>();
        for (Location loc : locationMap.values()) {
            if (loc.getProvince() != null && !loc.getProvince().isEmpty()) {
                provinces.add(loc.getProvince());
            }
        }
        return new ArrayList<>(provinces);
    }

    public void refreshGraph() {
        loadGraphFromDatabase();
    }

    private int getTotalEdges() {
        int count = 0;
        for (List<RouteEdge> edges : graph.values()) {
            count += edges.size();
        }
        return count;
    }
}