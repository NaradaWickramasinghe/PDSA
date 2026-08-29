package com.nibm.intelligenttravelmanagementsystem.networkanalysis.model;

import java.util.*;

/**
 * In-memory graph structure used purely for algorithmic computation.
 * Represented as an adjacency list (Map of nodeId -> list of weighted edges).
 *
 * <p>Design rationale:
 * <ul>
 *   <li>Adjacency list chosen over adjacency matrix because the travel graph
 *       is sparse (E ≈ O(V), not O(V²)), so O(V+E) space is much better than O(V²).</li>
 *   <li>Map&lt;String, List&lt;GraphEdge&gt;&gt; gives O(1) average lookup to a node's
 *       neighbor list and O(degree) iteration — the exact access pattern
 *       Brandes' algorithm needs.</li>
 *   <li>This structure is deliberately separate from JPA entities (Node, Edge) —
 *       the graph is a computation-only, in-memory structure built from the database,
 *       not a database structure itself.</li>
 * </ul>
 *
 * <p>Time complexity of operations:
 * <ul>
 *   <li>addNode: O(1) amortized</li>
 *   <li>addEdge: O(1) amortized</li>
 *   <li>neighborsOf: O(1) lookup + O(degree) iteration</li>
 *   <li>allNodeIds: O(1)</li>
 * </ul>
 */
public class TravelGraph {

    /** Adjacency list: nodeId → list of (neighborId, weight). */
    private final Map<String, List<GraphEdge>> adjacency = new HashMap<>();

    /** Set of all node IDs in the graph. */
    private final Set<String> nodeIds = new HashSet<>();

    /** Total number of directed edge entries (each undirected edge counts as 2). */
    private int directedEdgeCount = 0;

    /**
     * An immutable weighted edge in the graph.
     * Uses Java 17 record for lightweight, immutable value semantics.
     *
     * @param targetId the ID of the node this edge points to
     * @param weight   the edge weight (distance in km, travel time in minutes, or cost in LKR)
     */
    public record GraphEdge(String targetId, double weight) {}

    /**
     * Adds a node to the graph. If the node already exists, this is a no-op.
     *
     * @param id the unique identifier of the node (matches nodes.node_id in the database)
     */
    public void addNode(String id) {
        nodeIds.add(id);
        adjacency.putIfAbsent(id, new ArrayList<>());
    }

    /**
     * Adds an undirected weighted edge between two nodes.
     * Both nodes are implicitly added if they don't already exist.
     * Since the travel graph is undirected (a road from A to B implies B to A),
     * this method adds the edge in both directions.
     *
     * @param fromId the source node ID
     * @param toId   the target node ID
     * @param weight the edge weight (must be positive)
     */
    public void addEdge(String fromId, String toId, double weight) {
        addNode(fromId);
        addNode(toId);
        adjacency.get(fromId).add(new GraphEdge(toId, weight));
        adjacency.get(toId).add(new GraphEdge(fromId, weight));
        directedEdgeCount += 2;
    }

    /**
     * Returns the list of neighbors (outgoing edges) for a given node.
     *
     * @param nodeId the node to query
     * @return list of GraphEdge representing neighbors and weights; empty list if node not found
     */
    public List<GraphEdge> neighborsOf(String nodeId) {
        return adjacency.getOrDefault(nodeId, Collections.emptyList());
    }

    /**
     * Returns an unmodifiable view of all node IDs in the graph.
     *
     * @return set of all node IDs
     */
    public Set<String> allNodeIds() {
        return Collections.unmodifiableSet(nodeIds);
    }

    /**
     * Returns the total number of nodes (vertices) in the graph.
     *
     * @return node count |V|
     */
    public int nodeCount() {
        return nodeIds.size();
    }

    /**
     * Returns the total number of undirected edges in the graph.
     * Since each undirected edge is stored twice (once per direction),
     * this returns directedEdgeCount / 2.
     *
     * @return undirected edge count |E|
     */
    public int edgeCount() {
        return directedEdgeCount / 2;
    }
}
