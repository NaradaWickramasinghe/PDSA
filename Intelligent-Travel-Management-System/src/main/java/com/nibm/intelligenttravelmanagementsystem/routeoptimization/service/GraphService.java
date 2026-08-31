package com.nibm.intelligenttravelmanagementsystem.routeoptimization.service;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.Location;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.RouteEdge;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.repository.NodeRepository;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.repository.EdgeRepository;
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

    // Internal graph structure - using the Long ID from the models
    private Map<Long, List<RouteEdge>> graph = new HashMap<>();
    private Map<Long, Location> locationMap = new HashMap<>();
    private Map<String, Location> locationNameMap = new HashMap<>();
    private Map<String, Long> nodeIdToIdMap = new HashMap<>(); // Maps nodeId (String) to id (Long)

    @PostConstruct
    public void init() {
        loadGraphFromDatabase();
    }

    public void loadGraphFromDatabase() {
        log.info("🔄 Loading graph from database using shared models...");

        // Clear existing data
        graph.clear();
        locationMap.clear();
        locationNameMap.clear();
        nodeIdToIdMap.clear();

        // 1. Load all nodes
        List<Node> nodes = nodeRepository.findAll();

        if (nodes.isEmpty()) {
            log.warn("⚠️ No nodes found in database!");
            return;
        }

        // Convert Node to Location
        for (Node node : nodes) {
            // Use the Long 'id' field as our internal ID
            Long internalId = node.getId();
            String nodeId = node.getNodeId();

            if (internalId == null) {
                log.warn("⚠️ Node {} has null internal ID, skipping...", nodeId);
                continue;
            }

            // Map nodeId (String) to internal ID (Long) for edge lookups
            nodeIdToIdMap.put(nodeId, internalId);

            Location loc = new Location(
                    internalId,
                    node.getName() != null ? node.getName() : "Unknown",
                    node.getLatitude() != null ? node.getLatitude() : 0.0,
                    node.getLongitude() != null ? node.getLongitude() : 0.0,
                    node.getNodeType() != null ? node.getNodeType() : "unknown"
            );

            // Add additional fields
            loc.setProvince(node.getProvince());
            loc.setDistrict(node.getDistrict());
            loc.setDescription(node.getDescription());

            locationMap.put(internalId, loc);
            locationNameMap.put(node.getName().toLowerCase(), loc);
            graph.put(internalId, new ArrayList<>());
        }

        log.info("✅ Loaded {} locations from shared Node model", locationMap.size());

        // 2. Load all edges
        List<Edge> sharedEdges = edgeRepository.findAll();

        if (sharedEdges.isEmpty()) {
            log.warn("⚠️ No edges found in database!");
            return;
        }

        int edgeCount = 0;
        int skippedCount = 0;

        for (Edge sharedEdge : sharedEdges) {
            // Get source and target IDs
            // Use sourceNodeId and targetNodeId (Long) for internal mapping
            Long sourceId = sharedEdge.getSourceNodeId();
            Long targetId = sharedEdge.getTargetNodeId();

            // If source/target are null, try using the string-based source/destination
            if (sourceId == null || targetId == null) {
                String sourceNodeId = sharedEdge.getSource();
                String targetNodeId = sharedEdge.getDestination();

                if (sourceNodeId != null && targetNodeId != null) {
                    sourceId = nodeIdToIdMap.get(sourceNodeId);
                    targetId = nodeIdToIdMap.get(targetNodeId);
                }
            }

            if (sourceId == null || targetId == null) {
                log.debug("⚠️ Skipping edge {}: source or target not found", sharedEdge.getEdgeId());
                skippedCount++;
                continue;
            }

            Location source = locationMap.get(sourceId);
            Location target = locationMap.get(targetId);

            if (source == null || target == null) {
                log.debug("⚠️ Skipping edge {}: source or target location not in graph", sharedEdge.getEdgeId());
                skippedCount++;
                continue;
            }

            // Get edge properties with null safety
            Double distance = sharedEdge.getDistanceKm() != null ? sharedEdge.getDistanceKm() :
                    (sharedEdge.getDistance() != null ? sharedEdge.getDistance() : 10.0);

            Integer travelTime = sharedEdge.getTravelTimeMinutes();
            if (travelTime == null && sharedEdge.getTravelTime() != null) {
                travelTime = sharedEdge.getTravelTime().intValue();
            }
            if (travelTime == null) {
                travelTime = 30; // Default
            }

            Short riskLevelShort = sharedEdge.getRiskLevel();
            int riskLevel = riskLevelShort != null ? riskLevelShort.intValue() : 1;

            String transportMode = sharedEdge.getTransportMode() != null ?
                    sharedEdge.getTransportMode() : "road";

            // Adjust travel time based on transport mode
            int adjustedTime = travelTime;
            if (transportMode != null) {
                switch (transportMode.toLowerCase()) {
                    case "highway":
                        adjustedTime = (int)(travelTime * 0.8);
                        break;
                    case "local":
                        adjustedTime = (int)(travelTime * 1.2);
                        break;
                    default:
                        break;
                }
            }

            // Get additional fields with null safety
            Double estimatedCost = sharedEdge.getEstimatedCostLkr() != null ?
                    sharedEdge.getEstimatedCostLkr().doubleValue() : null;

            Integer roadQuality = sharedEdge.getRoadQuality() != null ?
                    sharedEdge.getRoadQuality().intValue() : null;

            Integer trafficLevel = sharedEdge.getTrafficLevel() != null ?
                    sharedEdge.getTrafficLevel().intValue() : null;

            Integer accessibility = sharedEdge.getAccessibility() != null ?
                    sharedEdge.getAccessibility().intValue() : null;

            // Use the internal Long ID for the edge
            Long edgeInternalId = sharedEdge.getId();
            if (edgeInternalId == null) {
                edgeInternalId = System.currentTimeMillis() + edgeCount; // Fallback unique ID
            }

            // Create RouteEdge (source -> target)
            RouteEdge routeEdge = new RouteEdge(
                    edgeInternalId,
                    source,
                    target,
                    distance,
                    adjustedTime,
                    riskLevel,
                    transportMode,
                    false // Default to bidirectional
            );

            // Add additional fields
            routeEdge.setEstimatedCostLkr(estimatedCost);
            routeEdge.setRoadQuality(roadQuality);
            routeEdge.setTrafficLevel(trafficLevel);
            routeEdge.setAccesibility(accessibility);

            // Add to graph
            graph.get(sourceId).add(routeEdge);
            edgeCount++;

            // Add reverse edge (bidirectional)
            RouteEdge reverseEdge = new RouteEdge(
                    edgeInternalId,
                    target,
                    source,
                    distance,
                    adjustedTime,
                    riskLevel,
                    transportMode,
                    false
            );
            reverseEdge.setEstimatedCostLkr(estimatedCost);
            reverseEdge.setRoadQuality(roadQuality);
            reverseEdge.setTrafficLevel(trafficLevel);
            reverseEdge.setAccesibility(accessibility);

            graph.get(targetId).add(reverseEdge);
            edgeCount++;
        }

        log.info("✅ Loaded {} edges from shared Edge model", sharedEdges.size());
        if (skippedCount > 0) {
            log.warn("⚠️ Skipped {} edges due to missing source/target nodes", skippedCount);
        }
        log.info("📊 Graph summary: {} locations, {} total connections",
                locationMap.size(), getTotalEdges());
    }

    public void refreshGraph() {
        loadGraphFromDatabase();
    }

    // Getter methods
    public Map<Long, List<RouteEdge>> getGraph() {
        return graph;
    }

    public Location getLocation(Long id) {
        return locationMap.get(id);
    }

    public Location getLocationByName(String name) {
        return name != null ? locationNameMap.get(name.toLowerCase()) : null;
    }

    public List<Location> getAllLocations() {
        return new ArrayList<>(locationMap.values());
    }

    public List<Location> getLocationsByProvince(String province) {
        List<Node> nodes = nodeRepository.findByProvince(province);
        return nodes.stream()
                .map(node -> locationMap.get(node.getId()))
                .filter(Objects::nonNull)
                .toList();
    }

    public List<Location> getLocationsByType(String type) {
        List<Node> nodes = nodeRepository.findByNodeType(type);
        return nodes.stream()
                .map(node -> locationMap.get(node.getId()))
                .filter(Objects::nonNull)
                .toList();
    }

    public List<String> getProvinces() {
        return nodeRepository.findAll().stream()
                .map(Node::getProvince)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private int getTotalEdges() {
        int count = 0;
        for (List<RouteEdge> edges : graph.values()) {
            count += edges.size();
        }
        return count;
    }
}