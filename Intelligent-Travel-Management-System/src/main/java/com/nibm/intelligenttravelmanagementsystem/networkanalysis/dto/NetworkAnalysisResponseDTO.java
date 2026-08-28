package com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto;

import java.util.List;

/**
 * Data Transfer Object for the complete network analysis response.
 *
 * <p>Contains metadata about the graph (node/edge counts, weight used,
 * computation time) and two independently-sorted rankings of all destinations:
 * one by betweenness centrality and one by closeness centrality.
 *
 * @param nodeCount           total number of destinations (nodes) in the graph
 * @param edgeCount           total number of travel routes (undirected edges) in the graph
 * @param weightUsed          which edge weight was used for computation
 *                            (distance_km, travel_time_minutes, or estimated_cost_lkr)
 * @param computationTimeMs   server-side wall-clock time to build the graph and run
 *                            Brandes' algorithm, in milliseconds
 * @param rankedByBetweenness all destinations sorted by betweenness centrality (highest first)
 * @param rankedByCloseness   all destinations sorted by closeness centrality (highest first)
 */
public record NetworkAnalysisResponseDTO(
        int nodeCount,
        int edgeCount,
        String weightUsed,
        long computationTimeMs,
        List<CentralityScoreDTO> rankedByBetweenness,
        List<CentralityScoreDTO> rankedByCloseness
) {
}
