package com.nibm.intelligenttravelmanagementsystem.networkanalysis.model;

import java.util.*;

public class TravelGraph {
    private final Map<String, List<GraphEdge>> adjacency = new HashMap<>();// one node id and that relevent all the other nodes that connected to that specific node 
    private final Set<String> nodeIds = new HashSet<>(); // set of node ids ex kand , col , mat , nuwra eli

    // Inner records for graph edges
    public record GraphEdge(String targetId, double weight) {}
    public record Edge(String targetId, double weight) {}

    public void addNode(String id) {
        nodeIds.add(id);
        adjacency.putIfAbsent(id, new ArrayList<>());
    }

    public void addEdge(String fromId, String toId, double weight) {
        addNode(fromId);
        addNode(toId);
        adjacency.get(fromId).add(new GraphEdge(toId, weight));
        adjacency.get(toId).add(new GraphEdge(fromId, weight));
    }

    public List<GraphEdge> neighborsOf(String nodeId) {
        return adjacency.getOrDefault(nodeId, Collections.emptyList());
    }

    public Set<String> allNodeIds() {
        return nodeIds;
    }

    public int nodeCount() {
        return nodeIds.size();
    }

    // Counts unique undirected edges in the graph
    public int edgeCount() {
        int totalEdges = 0;
        for (List<GraphEdge> edges : adjacency.values()) {
            totalEdges += edges.size();
        }
        return totalEdges / 2; // Divide by 2 because graph is undirected
    }
}