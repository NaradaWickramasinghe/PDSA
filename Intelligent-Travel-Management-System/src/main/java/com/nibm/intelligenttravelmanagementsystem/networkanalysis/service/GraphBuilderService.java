package com.nibm.intelligenttravelmanagementsystem.networkanalysis.service;

import com.nibm.intelligenttravelmanagementsystem.networkanalysis.model.TravelGraph;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.repository.NetworkEdgeRepository;
import com.nibm.intelligenttravelmanagementsystem.networkanalysis.repository.NetworkNodeRepository;
import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for converting database rows into an in-memory {@link TravelGraph}.
 *
 * <p>This service bridges the persistence layer (JPA entities) and the algorithmic
 * layer (TravelGraph adjacency list). It fetches all nodes and edges from the database
 * and constructs a computation-ready graph structure.
 *
 * <p>The weight used for edge construction is configurable via the {@code weightType}
 * parameter, supporting three metrics from the edges table:
 * <ul>
 *   <li>{@code distance_km} — geographic distance in kilometers</li>
 *   <li>{@code travel_time_minutes} — estimated travel time in minutes</li>
 *   <li>{@code estimated_cost_lkr} — estimated travel cost in Sri Lankan Rupees</li>
 * </ul>
 *
 * <p>Time complexity: O(V + E) where V = number of nodes, E = number of edges.
 * <br>Space complexity: O(V + E) for the constructed graph.
 */
@Service
@RequiredArgsConstructor
public class GraphBuilderService {

    private final NetworkNodeRepository nodeRepository;
    private final NetworkEdgeRepository edgeRepository;

    /** Valid weight type options for edge weight selection. */
    private static final List<String> VALID_WEIGHT_TYPES = List.of(
            "distance_km", "travel_time_minutes", "estimated_cost_lkr"
    );

    /**
     * Builds an in-memory TravelGraph from all nodes and edges in the database.
     *
     * @param weightType which edge column to use as the weight
     *                   ("distance_km", "travel_time_minutes", or "estimated_cost_lkr")
     * @return a fully constructed TravelGraph ready for centrality computation
     * @throws IllegalStateException    if the graph would be empty (no nodes in the database)
     * @throws IllegalArgumentException if the weightType is not one of the valid options
     */
    public TravelGraph buildGraph(String weightType) {
        // Validate weight type
        if (!VALID_WEIGHT_TYPES.contains(weightType)) {
            throw new IllegalArgumentException(
                    "Invalid weight type: '" + weightType + "'. Must be one of: " + VALID_WEIGHT_TYPES
            );
        }

        TravelGraph graph = new TravelGraph();

        // Load all nodes from the database and add them to the graph
        // Time: O(V), Space: O(V)
        nodeRepository.findAll()
                .forEach(node -> graph.addNode(node.getNodeId()));

        // Validate that the graph is non-empty
        if (graph.nodeCount() == 0) {
            throw new IllegalStateException(
                    "Cannot compute centrality on an empty graph — no nodes found in the database"
            );
        }

        // Load all edges and add them with the selected weight
        // Time: O(E), Space: O(E) for the adjacency lists
        edgeRepository.findAll().forEach(edge ->
                graph.addEdge(
                        edge.getSource(),
                        edge.getDestination(),
                        extractWeight(edge, weightType)
                )
        );

        return graph;
    }

    /**
     * Extracts the appropriate weight value from an Edge entity based on the weight type.
     *
     * @param edge       the edge entity to extract weight from
     * @param weightType the weight column to use
     * @return the weight value as a double
     */
    private double extractWeight(Edge edge, String weightType) {
        return switch (weightType) {
            case "distance_km" -> edge.getDistanceKm();
            case "travel_time_minutes" -> edge.getTravelTimeMinutes().doubleValue();
            case "estimated_cost_lkr" -> edge.getEstimatedCostLkr().doubleValue();
            default -> edge.getDistanceKm(); // fallback, should not be reached due to validation
        };
    }
}
