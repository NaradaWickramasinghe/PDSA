package com.nibm.intelligenttravelmanagementsystem.optimization.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.DestinationRecommendation;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationRequest;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationResponse;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.RecommendationService;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto.CentralityScoreDTO;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto.NetworkAnalysisResponseDTO;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.service.NetworkAnalysisService;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.ModuleContributionDTO;
import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.IntegratedCandidate;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.TravelGraph;
import com.nibm.intelligenttravelmanagementsystem.optimization.model.TravelNode;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationRequest;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.dto.ResourceAllocationResponse;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.service.ResourceAllocationService;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.dto.RouteResult;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.model.TransportMode;
import com.nibm.intelligenttravelmanagementsystem.routeoptimization.service.RouteOptimizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service that orchestrates and fetches outputs from Modules 1, 2, 3, and 4
 * to feed the Multi-Objective Optimization engine in Module 5.
 */
@Slf4j
@Service
public class ModuleIntegrationService {

    private final RecommendationService recommendationService; // Module 4
    private final NetworkAnalysisService networkAnalysisService; // Module 3
    private final RouteOptimizationService routeOptimizationService; // Module 1
    private final ResourceAllocationService resourceAllocationService; // Module 2

    public ModuleIntegrationService(RecommendationService recommendationService,
                                    NetworkAnalysisService networkAnalysisService,
                                    RouteOptimizationService routeOptimizationService,
                                    ResourceAllocationService resourceAllocationService) {
        this.recommendationService = recommendationService;
        this.networkAnalysisService = networkAnalysisService;
        this.routeOptimizationService = routeOptimizationService;
        this.resourceAllocationService = resourceAllocationService;
    }

    /**
     * Collects and integrates candidate destinations by querying Modules 1, 2, 3, and 4.
     */
    public List<IntegratedCandidate> collectIntegratedCandidates(OptimizationRequest request, TravelGraph graph) {
        List<IntegratedCandidate> candidates = new ArrayList<>();
        String startNodeId = (request.getSourceNodeId() != null && !request.getSourceNodeId().isBlank())
                ? request.getSourceNodeId()
                : "CMB";

        // 1. Fetch Module 4 recommendations (Decision Tree + KNN)
        Map<String, DestinationRecommendation> m4Recommendations = fetchModule4Recommendations(request);

        // 2. Fetch Module 3 network topology and centrality metrics (Brandes Algorithm)
        Map<String, CentralityScoreDTO> m3CentralityMap = fetchModule3Centrality();

        // 3. For each candidate node, fetch Module 1 (Route) and Module 2 (Resources)
        Collection<TravelNode> availableNodes = graph.getAllNodes();

        for (TravelNode node : availableNodes) {
            String nodeId = node.getNodeId();
            if (nodeId.equalsIgnoreCase(startNodeId)) {
                continue; // Skip the start node itself
            }

            // A. Module 4: Match score & Decision Tree suitability
            DestinationRecommendation rec = matchNodeToRecommendation(node, m4Recommendations);
            double matchScore = rec != null ? rec.getScore() : 0.65;
            String suitability = (rec != null && rec.getSuitabilityLabel() != null)
                    ? rec.getSuitabilityLabel().name()
                    : "SUITABLE";

            // B. Module 3: Network Betweenness & Closeness Centrality
            CentralityScoreDTO cent = matchNodeToCentrality(node, m3CentralityMap);
            double betweenness = cent != null ? cent.betweenness() : 0.1;
            double closeness = cent != null ? cent.closeness() : 0.2;

            // C. Module 1: Routing shortest path, transit time, and risk
            RouteResult routeRes = null;
            try {
                routeRes = routeOptimizationService.findRoute(startNodeId, nodeId, TransportMode.NORMAL_VEHICLE, false, true, 3);
            } catch (Exception ex) {
                log.debug("Module 1 routing fallback for {} -> {}: {}", startNodeId, nodeId, ex.getMessage());
            }

            double transitDist = (routeRes != null && routeRes.getTotalDistanceKm() > 0)
                    ? routeRes.getTotalDistanceKm()
                    : graph.estimateHaversineDistanceKm(startNodeId, nodeId);
            double transitDuration = (routeRes != null && routeRes.getEstimatedTimeMinutes() > 0)
                    ? routeRes.getEstimatedTimeMinutes()
                    : (transitDist / 60.0) * 60.0;
            int riskLevel = (routeRes != null) ? (int) Math.round(routeRes.getRiskScore()) : 2;

            // D. Module 2: Resource Allocation for gear and equipment packages
            ResourceAllocationResponse allocRes = null;
            try {
                ResourceAllocationRequest resReq = ResourceAllocationRequest.builder()
                        .destination(node.getName())
                        .totalBudget(request.getMaxBudgetLkr() != null ? (double) request.getMaxBudgetLkr() : 40000.0)
                        .emergencyReserve(5000.0)
                        .availableHours(request.getMaxTimeMinutes() != null ? request.getMaxTimeMinutes() / 60.0 : 12.0)
                        .luggageCapacity(15.0)
                        .selectedAlgorithm("PIPELINE")
                        .build();
                allocRes = resourceAllocationService.allocateResources(resReq);
            } catch (Exception ex) {
                log.debug("Module 2 allocation fallback for {}: {}", node.getName(), ex.getMessage());
            }

            double resourceCost = (allocRes != null && allocRes.getTotalCost() > 0)
                    ? allocRes.getTotalCost()
                    : 3500.0;
            String allocatedPackage = (allocRes != null && allocRes.getSelectedResources() != null && !allocRes.getSelectedResources().isEmpty())
                    ? allocRes.getSelectedResources().get(0).getName()
                    : "Standard Safety & Gear Package";

            IntegratedCandidate candidate = IntegratedCandidate.builder()
                    .nodeId(nodeId)
                    .name(node.getName())
                    .province(node.getProvince())
                    .district(node.getDistrict())
                    .latitude(node.getLatitude())
                    .longitude(node.getLongitude())
                    .decisionSuitability(suitability)
                    .matchScore(matchScore)
                    .betweennessCentrality(betweenness)
                    .closenessCentrality(closeness)
                    .transitDistanceKm(Math.round(transitDist * 10.0) / 10.0)
                    .transitDurationMinutes(Math.round(transitDuration * 10.0) / 10.0)
                    .riskLevel(riskLevel)
                    .resourceCostLkr(resourceCost)
                    .allocatedPackage(allocatedPackage)
                    .resourceFeasible(allocRes == null || allocRes.isFeasible())
                    .build();

            candidate.computeCompositeMetrics(request.getCostWeight(), request.getSafetyWeight());
            candidates.add(candidate);
        }

        // Sort candidates by composite utility value in descending order
        candidates.sort(Comparator.comparingDouble(IntegratedCandidate::getCompositeValue).reversed());
        return candidates;
    }

    public List<ModuleContributionDTO> buildContributionDTOs(List<IntegratedCandidate> selectedCandidates) {
        List<ModuleContributionDTO> dtos = new ArrayList<>();
        if (selectedCandidates == null) return dtos;

        for (IntegratedCandidate c : selectedCandidates) {
            dtos.add(ModuleContributionDTO.builder()
                    .destinationId(c.getNodeId())
                    .destinationName(c.getName())
                    .module4Suitability(c.getDecisionSuitability())
                    .module4MatchScore(Math.round(c.getMatchScore() * 100.0) / 100.0)
                    .module3Betweenness(Math.round(c.getBetweennessCentrality() * 1000.0) / 1000.0)
                    .module3Closeness(Math.round(c.getClosenessCentrality() * 1000.0) / 1000.0)
                    .module1DistanceKm(c.getTransitDistanceKm())
                    .module1DurationMinutes(c.getTransitDurationMinutes())
                    .module1RiskLevel(c.getRiskLevel())
                    .module2AllocatedPackage(c.getAllocatedPackage())
                    .module2ResourceCostLkr(c.getResourceCostLkr())
                    .compositeUtilityScore(Math.round(c.getCompositeValue() * 1000.0) / 1000.0)
                    .build());
        }
        return dtos;
    }

    private Map<String, DestinationRecommendation> fetchModule4Recommendations(OptimizationRequest request) {
        Map<String, DestinationRecommendation> map = new HashMap<>();
        try {
            TravelStyle style = parseTravelStyle(request.getTravelStyle());
            double b = request.getMaxBudgetLkr() != null ? (double) request.getMaxBudgetLkr() : 35000.0;
            RecommendationRequest m4Req = RecommendationRequest.builder()
                    .budget(BigDecimal.valueOf(b))
                    .durationDays(request.getDurationDays() != null ? request.getDurationDays() : 3)
                    .groupSize(request.getGroupSize() != null ? request.getGroupSize() : 2)
                    .ageGroup(AgeGroup.YOUNG_ADULT)
                    .travelStyle(style)
                    .beachPreference(request.getBeachPreference() != null ? request.getBeachPreference() : 7)
                    .adventurePreference(request.getAdventurePreference() != null ? request.getAdventurePreference() : 8)
                    .naturePreference(request.getNaturePreference() != null ? request.getNaturePreference() : 9)
                    .culturePreference(request.getCulturePreference() != null ? request.getCulturePreference() : 6)
                    .nightlifePreference(request.getNightlifePreference() != null ? request.getNightlifePreference() : 4)
                    .relaxationPreference(request.getRelaxationPreference() != null ? request.getRelaxationPreference() : 8)
                    .topN(10)
                    .build();

            RecommendationResponse m4Res = recommendationService.getRecommendations(m4Req);
            if (m4Res != null && m4Res.getRecommendations() != null) {
                for (DestinationRecommendation rec : m4Res.getRecommendations()) {
                    if (rec.getDestination() != null) {
                        map.put(rec.getDestination().toLowerCase(), rec);
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Module 4 recommendation fetch skipped: {}", ex.getMessage());
        }
        return map;
    }

    private Map<String, CentralityScoreDTO> fetchModule3Centrality() {
        Map<String, CentralityScoreDTO> map = new HashMap<>();
        try {
            NetworkAnalysisResponseDTO netRes = networkAnalysisService.analyzeNetwork("travel_time_minutes");
            if (netRes != null && netRes.rankedByBetweenness() != null) {
                for (CentralityScoreDTO dto : netRes.rankedByBetweenness()) {
                    if (dto.nodeId() != null) map.put(dto.nodeId().toUpperCase(), dto);
                    if (dto.name() != null) map.put(dto.name().toLowerCase(), dto);
                }
            }
        } catch (Exception ex) {
            log.warn("Module 3 network analysis fetch skipped: {}", ex.getMessage());
        }
        return map;
    }

    private DestinationRecommendation matchNodeToRecommendation(TravelNode node, Map<String, DestinationRecommendation> map) {
        String name = node.getName().toLowerCase();
        for (Map.Entry<String, DestinationRecommendation> entry : map.entrySet()) {
            if (name.contains(entry.getKey()) || entry.getKey().contains(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private CentralityScoreDTO matchNodeToCentrality(TravelNode node, Map<String, CentralityScoreDTO> map) {
        if (map.containsKey(node.getNodeId().toUpperCase())) {
            return map.get(node.getNodeId().toUpperCase());
        }
        String name = node.getName().toLowerCase();
        for (Map.Entry<String, CentralityScoreDTO> entry : map.entrySet()) {
            if (name.contains(entry.getKey()) || entry.getKey().contains(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private TravelStyle parseTravelStyle(String style) {
        if (style == null || style.isBlank()) return TravelStyle.ADVENTURE;
        try {
            return TravelStyle.valueOf(style.trim().toUpperCase());
        } catch (Exception e) {
            return TravelStyle.ADVENTURE;
        }
    }
}
