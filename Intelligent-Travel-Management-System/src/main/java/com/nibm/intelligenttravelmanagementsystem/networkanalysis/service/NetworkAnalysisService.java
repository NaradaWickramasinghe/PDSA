package com.nibm.intelligenttravelmanagementsystem.networkanalysis.service;

import com.nibm.intelligenttravelmanagementsystem.networkanalysis.controller.LocationNotFoundException;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto.CentralityScoreDTO;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto.NetworkAnalysisResponseDTO;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.model.TravelGraph;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.repository.NetworkNodeRepository;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrating service for the Network Analysis module.
 *
 * <p>Coordinates the full analysis pipeline:
 * <ol>
 *   <li>Graph construction from DB (via {@link GraphBuilderService})</li>
 *   <li>Centrality computation (via {@link CentralityService})</li>
 *   <li>Result assembly into DTOs with human-readable location names</li>
 * </ol>
 *
 * <p>This service is the single entry point for all controller endpoints.
 * All endpoints call through this service, ensuring consistent behavior
 * and a single place to add caching if needed in the future.
 *
 * <p><b>Recomputation note:</b> Currently, centrality scores are recomputed
 * on every request. At the scale of a national travel network (hundreds of nodes),
 * this completes in sub-second time. For future optimization, annotate
 * {@link #analyzeNetwork(String)} with {@code @Cacheable("networkAnalysis")}
 * and invalidate via {@code @CacheEvict} when routes are added/removed.
 */
@Service
@RequiredArgsConstructor
public class NetworkAnalysisService {

    private final GraphBuilderService graphBuilderService;
    private final CentralityService centralityService;
    private final NetworkNodeRepository nodeRepository;

    /**
     * Performs a full network analysis: builds the graph, runs Brandes' algorithm,
     * and returns both betweenness and closeness rankings.
     *
     * @param weightType which edge weight to use ("distance_km", "travel_time_minutes",
     *                   or "estimated_cost_lkr")
     * @return complete analysis response with both rankings sorted descending
     * @throws IllegalStateException    if the graph is empty
     * @throws IllegalArgumentException if weightType is invalid
     */
    public NetworkAnalysisResponseDTO analyzeNetwork(String weightType) {
        long startNanos = System.nanoTime();

        // Step 1: Build the in-memory graph from database
        TravelGraph graph = graphBuilderService.buildGraph(weightType);

        // Step 2: Run Brandes' algorithm — computes both metrics in one pass
        CentralityService.CentralityResult result = centralityService.computeCentrality(graph);

        // Step 3: Build a name lookup map (nodeId → display name)
        Map<String, String> namesByNodeId = nodeRepository.findAll().stream()
                .collect(Collectors.toMap(Node::getNodeId, Node::getName));

        // Step 4: Assemble CentralityScoreDTO list for all nodes
        List<CentralityScoreDTO> scores = graph.allNodeIds().stream()
                .map(nodeId -> new CentralityScoreDTO(
                        nodeId,
                        namesByNodeId.getOrDefault(nodeId, "Unknown"),
                        result.betweenness().getOrDefault(nodeId, 0.0),
                        result.closeness().getOrDefault(nodeId, 0.0)
                ))
                .toList();

        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

        // Step 5: Return response with two independently-sorted rankings
        return new NetworkAnalysisResponseDTO(
                graph.nodeCount(),
                graph.edgeCount(),
                weightType,
                elapsedMs,
                scores.stream()
                        .sorted(Comparator.comparingDouble(CentralityScoreDTO::betweenness).reversed())
                        .toList(),
                scores.stream()
                        .sorted(Comparator.comparingDouble(CentralityScoreDTO::closeness).reversed())
                        .toList()
        );
    }

    /**
     * Returns the centrality scores for a single destination.
     *
     * <p>Internally runs the full analysis (since Brandes' algorithm must process
     * all nodes to produce any individual result) and filters for the requested node.
     *
     * @param nodeId     the node_id to look up
     * @param weightType which edge weight to use
     * @return centrality scores for the specified node
     * @throws LocationNotFoundException if no node with the given ID exists in the results
     */
    public CentralityScoreDTO getScoreForLocation(String nodeId, String weightType) {
        NetworkAnalysisResponseDTO fullResult = analyzeNetwork(weightType);

        return fullResult.rankedByBetweenness().stream()
                .filter(score -> score.nodeId().equals(nodeId))
                .findFirst()
                .orElseThrow(() -> new LocationNotFoundException(nodeId));
    }
}
