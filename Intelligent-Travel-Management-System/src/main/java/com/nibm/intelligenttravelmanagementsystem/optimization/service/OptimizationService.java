package com.nibm.intelligenttravelmanagementsystem.optimization.service;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.*;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.*;
import com.nibm.intelligenttravelmanagementsystem.optimization.repository.OptimizationEdgeRepository;
import com.nibm.intelligenttravelmanagementsystem.optimization.repository.OptimizationNodeRepository;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class OptimizationService {

    private final OptimizationNodeRepository nodeRepository;
    private final OptimizationEdgeRepository edgeRepository;
    private final BranchAndBoundOptimizer branchAndBoundOptimizer;
    private final ParetoFrontierOptimizer paretoFrontierOptimizer;
    private final GeneticRouteOptimizer geneticRouteOptimizer;

    private TravelGraph cachedGraph;

    public OptimizationService(OptimizationNodeRepository nodeRepository,
                               OptimizationEdgeRepository edgeRepository,
                               BranchAndBoundOptimizer branchAndBoundOptimizer,
                               ParetoFrontierOptimizer paretoFrontierOptimizer,
                               GeneticRouteOptimizer geneticRouteOptimizer) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.branchAndBoundOptimizer = branchAndBoundOptimizer;
        this.paretoFrontierOptimizer = paretoFrontierOptimizer;
        this.geneticRouteOptimizer = geneticRouteOptimizer;
    }

    @PostConstruct
    public void init() {
        refreshGraph();
    }

    public synchronized TravelGraph refreshGraph() {
        TravelGraph graph = new TravelGraph();
        try {
            List<Node> dbNodes = nodeRepository.findAll();
            List<Edge> dbEdges = edgeRepository.findAll();

            if (!dbNodes.isEmpty() && !dbEdges.isEmpty()) {
                log.info("Loading {} nodes and {} edges from Supabase...", dbNodes.size(), dbEdges.size());
                for (Node n : dbNodes) {
                    graph.addNode(TravelNode.builder()
                            .nodeId(n.getNodeId())
                            .name(n.getName())
                            .nodeType(n.getNodeType())
                            .province(n.getProvince())
                            .district(n.getDistrict())
                            .latitude(n.getLatitude())
                            .longitude(n.getLongitude())
                            .description(n.getDescription())
                            .build());
                }

                for (Edge e : dbEdges) {
                    TravelEdge forwardEdge = TravelEdge.builder()
                            .edgeId(e.getEdgeId())
                            .source(e.getSource())
                            .destination(e.getDestination())
                            .distanceKm(e.getDistanceKm() != null ? e.getDistanceKm() : 10.0)
                            .travelTimeMinutes(e.getTravelTimeMinutes() != null ? e.getTravelTimeMinutes() : 30)
                            .estimatedCostLkr(e.getEstimatedCostLkr() != null ? e.getEstimatedCostLkr() : 500)
                            .roadQuality(e.getRoadQuality() != null ? e.getRoadQuality() : 3)
                            .trafficLevel(e.getTrafficLevel() != null ? e.getTrafficLevel() : 2)
                            .transportMode(e.getTransportMode() != null ? e.getTransportMode() : "ROAD")
                            .accessibility(e.getAccessibility() != null ? e.getAccessibility() : 3)
                            .riskLevel(e.getRiskLevel() != null ? e.getRiskLevel() : 2)
                            .build();
                    graph.addEdge(forwardEdge);

                    if (!"AIR".equalsIgnoreCase(e.getTransportMode()) && !"FERRY".equalsIgnoreCase(e.getTransportMode())) {
                        TravelEdge reverseEdge = TravelEdge.builder()
                                .edgeId(e.getEdgeId() + "_REV")
                                .source(e.getDestination())
                                .destination(e.getSource())
                                .distanceKm(forwardEdge.getDistanceKm())
                                .travelTimeMinutes(forwardEdge.getTravelTimeMinutes())
                                .estimatedCostLkr(forwardEdge.getEstimatedCostLkr())
                                .roadQuality(forwardEdge.getRoadQuality())
                                .trafficLevel(forwardEdge.getTrafficLevel())
                                .transportMode(forwardEdge.getTransportMode())
                                .accessibility(forwardEdge.getAccessibility())
                                .riskLevel(forwardEdge.getRiskLevel())
                                .build();
                        graph.addEdge(reverseEdge);
                    }
                }
            } else {
                populateDefaultNetwork(graph);
            }
        } catch (Exception ex) {
            log.warn("Database connection unavailable or empty, using in-memory network: {}", ex.getMessage());
            populateDefaultNetwork(graph);
        }

        this.cachedGraph = graph;
        return graph;
    }

    public TravelGraph getGraph() {
        if (cachedGraph == null || cachedGraph.getNodeCount() == 0) {
            return refreshGraph();
        }
        return cachedGraph;
    }

    public OptimizationResponse planOptimalRoute(OptimizationRequest request) {
        TravelGraph graph = getGraph();
        request.normalizeWeights();

        OptimizationAlgorithm algorithm = selectAlgorithm(request.getAlgorithm());
        OptimizationResult result = algorithm.optimize(graph, request);

        if (!result.isSuccess() || result.getBestRoute() == null) {
            return OptimizationResponse.builder()
                    .success(false)
                    .message(result.getMessage())
                    .selectedAlgorithm(algorithm.getName())
                    .sourceNodeId(request.getSourceNodeId())
                    .destinationNodeId(request.getDestinationNodeId())
                    .executionTimeMs(result.getExecutionTimeMs())
                    .memoryUsedKb(result.getMemoryUsedKb())
                    .nodesExploredCount(result.getNodesExplored())
                    .build();
        }

        RouteSummaryDTO bestRouteDTO = mapToRouteSummaryDTO("Optimal Recommendation", result.getBestRoute(), graph);

        List<RouteSummaryDTO> paretoDTOs = new ArrayList<>();
        if (result.getParetoAlternatives() != null) {
            int idx = 1;
            for (RouteCandidate alt : result.getParetoAlternatives()) {
                String label = "Alternative Option #" + idx++;
                if (alt.getTotalDurationMinutes() < bestRouteDTO.getTotalDurationMinutes()) {
                    label = "Faster Alternative";
                } else if (alt.getTotalCostLkr() < bestRouteDTO.getTotalCostLkr()) {
                    label = "Cheaper Alternative";
                } else if (alt.getAverageRiskLevel() < bestRouteDTO.getAverageRiskLevel()) {
                    label = "Safer Alternative";
                }
                paretoDTOs.add(mapToRouteSummaryDTO(label, alt, graph));
            }
        }

        return OptimizationResponse.builder()
                .success(true)
                .message(result.getMessage())
                .selectedAlgorithm(algorithm.getName())
                .sourceNodeId(request.getSourceNodeId())
                .destinationNodeId(request.getDestinationNodeId())
                .bestRoute(bestRouteDTO)
                .paretoAlternatives(paretoDTOs)
                .executionTimeMs(result.getExecutionTimeMs())
                .memoryUsedKb(result.getMemoryUsedKb())
                .nodesExploredCount(result.getNodesExplored())
                .build();
    }

    private OptimizationAlgorithm selectAlgorithm(AlgorithmType type) {
        if (type == null) return branchAndBoundOptimizer;
        return switch (type) {
            case PARETO_DYNAMIC_PROGRAMMING -> paretoFrontierOptimizer;
            case GENETIC_ALGORITHM -> geneticRouteOptimizer;
            case BRANCH_AND_BOUND -> branchAndBoundOptimizer;
        };
    }

    private RouteSummaryDTO mapToRouteSummaryDTO(String label, RouteCandidate candidate, TravelGraph graph) {
        List<String> nodeNames = new ArrayList<>();
        for (String nodeId : candidate.getNodeIds()) {
            TravelNode node = graph.getNode(nodeId);
            nodeNames.add(node != null ? node.getName() : nodeId);
        }

        return RouteSummaryDTO.builder()
                .label(label)
                .pathNodeIds(candidate.getNodeIds())
                .pathNodeNames(nodeNames)
                .edges(candidate.getEdges())
                .totalDistanceKm(Math.round(candidate.getTotalDistanceKm() * 10.0) / 10.0)
                .totalDurationMinutes(Math.round(candidate.getTotalDurationMinutes() * 10.0) / 10.0)
                .totalCostLkr(candidate.getTotalCostLkr())
                .averageRiskLevel(Math.round(candidate.getAverageRiskLevel() * 100.0) / 100.0)
                .averageRoadQuality(Math.round(candidate.getAverageRoadQuality() * 100.0) / 100.0)
                .compositeScore(Math.round(candidate.getCompositeScore() * 1000.0) / 1000.0)
                .build();
    }

    public void populateDefaultNetwork(TravelGraph graph) {
        addNodeHelper(graph, "CMB", "Colombo Fort", "CITY", "Western", "Colombo", 6.9344, 79.8428);
        addNodeHelper(graph, "NEG", "Negombo Beach", "TOURIST_DESTINATION", "Western", "Gampaha", 7.2008, 79.8737);
        addNodeHelper(graph, "KDY", "Kandy City", "TRANSPORT_HUB", "Central", "Kandy", 7.2906, 80.6337);
        addNodeHelper(graph, "SIG", "Sigiriya Rock", "TOURIST_DESTINATION", "Central", "Matale", 7.9570, 80.7603);
        addNodeHelper(graph, "NUE", "Nuwara Eliya", "TOURIST_DESTINATION", "Central", "Nuwara Eliya", 6.9497, 80.7891);
        addNodeHelper(graph, "HOR", "Horton Plains", "TOURIST_DESTINATION", "Central", "Nuwara Eliya", 6.8028, 80.8044);
        addNodeHelper(graph, "ADM", "Adam's Peak", "TOURIST_DESTINATION", "Sabaragamuwa", "Ratnapura", 6.8096, 80.4994);
        addNodeHelper(graph, "ELL", "Ella Town", "TOURIST_DESTINATION", "Uva", "Badulla", 6.8667, 81.0466);
        addNodeHelper(graph, "YAL", "Yala Park", "TOURIST_DESTINATION", "Southern", "Hambantota", 6.3725, 81.5165);
        addNodeHelper(graph, "GLL", "Galle Fort", "CITY", "Southern", "Galle", 6.0535, 80.2210);
        addNodeHelper(graph, "MIR", "Mirissa Coast", "TOURIST_DESTINATION", "Southern", "Matara", 5.9483, 80.4578);
        addNodeHelper(graph, "TRI", "Trincomalee", "PORT", "Eastern", "Trincomalee", 8.5874, 81.2152);
        addNodeHelper(graph, "JAF", "Jaffna Fort", "CITY", "Northern", "Jaffna", 9.6615, 80.0255);

        addEdgeHelper(graph, "E1", "CMB", "NEG", 38.0, 45, 1200, 4, 3, "HIGHWAY", 5, 1);
        addEdgeHelper(graph, "E2", "CMB", "KDY", 115.0, 190, 2500, 3, 4, "ROAD", 4, 2);
        addEdgeHelper(graph, "E3", "CMB", "KDY", 120.0, 160, 1800, 4, 1, "RAIL", 5, 1);
        addEdgeHelper(graph, "E4", "CMB", "GLL", 125.0, 95, 3200, 5, 2, "HIGHWAY", 5, 1);
        addEdgeHelper(graph, "E5", "KDY", "SIG", 90.0, 140, 2200, 3, 2, "ROAD", 3, 2);
        addEdgeHelper(graph, "E6", "KDY", "NUE", 75.0, 150, 2000, 3, 3, "ROAD", 3, 3);
        addEdgeHelper(graph, "E7", "NUE", "HOR", 32.0, 75, 1500, 2, 1, "ROAD", 2, 4);
        addEdgeHelper(graph, "E8", "HOR", "ELL", 48.0, 110, 1800, 3, 2, "ROAD", 3, 3);
        addEdgeHelper(graph, "E9", "NUE", "ELL", 55.0, 120, 1200, 4, 1, "RAIL", 4, 1);
        addEdgeHelper(graph, "E10", "ELL", "YAL", 95.0, 150, 3500, 3, 2, "ROAD", 3, 2);
        addEdgeHelper(graph, "E11", "GLL", "MIR", 35.0, 45, 1000, 4, 3, "ROAD", 5, 1);
        addEdgeHelper(graph, "E12", "MIR", "YAL", 110.0, 140, 2800, 4, 2, "HIGHWAY", 4, 1);
        addEdgeHelper(graph, "E13", "SIG", "TRI", 100.0, 130, 2400, 3, 2, "ROAD", 4, 2);
        addEdgeHelper(graph, "E14", "TRI", "JAF", 230.0, 290, 4500, 3, 2, "HIGHWAY", 4, 2);
        addEdgeHelper(graph, "E15", "KDY", "ADM", 85.0, 180, 2200, 2, 2, "ROAD", 2, 4);
        addEdgeHelper(graph, "E16", "ADM", "NUE", 65.0, 160, 2000, 2, 1, "ROAD", 2, 4);
    }

    private void addNodeHelper(TravelGraph g, String id, String name, String type, String prov, String dist, double lat, double lon) {
        g.addNode(TravelNode.builder()
                .nodeId(id)
                .name(name)
                .nodeType(type)
                .province(prov)
                .district(dist)
                .latitude(lat)
                .longitude(lon)
                .build());
    }

    private void addEdgeHelper(TravelGraph g, String id, String u, String v, double dist, int time, int cost, int qual, int traf, String mode, int acc, int risk) {
        g.addEdge(TravelEdge.builder()
                .edgeId(id + "_F")
                .source(u)
                .destination(v)
                .distanceKm(dist)
                .travelTimeMinutes(time)
                .estimatedCostLkr(cost)
                .roadQuality(qual)
                .trafficLevel(traf)
                .transportMode(mode)
                .accessibility(acc)
                .riskLevel(risk)
                .build());

        g.addEdge(TravelEdge.builder()
                .edgeId(id + "_R")
                .source(v)
                .destination(u)
                .distanceKm(dist)
                .travelTimeMinutes(time)
                .estimatedCostLkr(cost)
                .roadQuality(qual)
                .trafficLevel(traf)
                .transportMode(mode)
                .accessibility(acc)
                .riskLevel(risk)
                .build());
    }
}
