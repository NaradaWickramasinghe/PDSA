package com.nibm.intelligenttravelmanagementsystem.networkanalysis.dto;

/**
 * Data Transfer Object representing a single destination's centrality scores.
 *
 * <p>Returned as part of the network analysis response. Contains both
 * betweenness and closeness centrality scores regardless of which
 * ranking the entry appears in, so consumers can display both metrics
 * for any destination.
 *
 * @param nodeId      the unique identifier of the destination (matches nodes.node_id)
 * @param name        the display name of the destination
 * @param betweenness the betweenness centrality score — higher values indicate
 *                    the destination acts as a gateway/bridge between regions
 * @param closeness   the closeness centrality score — higher values indicate
 *                    the destination is well-positioned to reach all others quickly
 * @param latitude    the latitude coordinate of the destination for map visualization
 * @param longitude   the longitude coordinate of the destination for map visualization
 */
public record CentralityScoreDTO(
        String nodeId,
        String name,
        double betweenness,
        double closeness,
        Double latitude,
        Double longitude
) {
}
