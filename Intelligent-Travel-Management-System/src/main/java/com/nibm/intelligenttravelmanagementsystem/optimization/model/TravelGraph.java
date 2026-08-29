package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import java.util.*;

public class TravelGraph {
    private final Map<String, TravelNode> nodes = new HashMap<>();
    private final Map<String, List<TravelEdge>> adjacencyList = new HashMap<>();

    public void addNode(TravelNode node) {
        nodes.put(node.getNodeId(), node);
        adjacencyList.putIfAbsent(node.getNodeId(), new ArrayList<>());
    }

    public void addEdge(TravelEdge edge) {
        if (!nodes.containsKey(edge.getSource()) || !nodes.containsKey(edge.getDestination())) {
            nodes.putIfAbsent(edge.getSource(), TravelNode.builder().nodeId(edge.getSource()).name(edge.getSource()).build());
            nodes.putIfAbsent(edge.getDestination(), TravelNode.builder().nodeId(edge.getDestination()).name(edge.getDestination()).build());
        }
        adjacencyList.computeIfAbsent(edge.getSource(), k -> new ArrayList<>()).add(edge);
    }

    public TravelNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public List<TravelEdge> getOutgoingEdges(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
    }

    public boolean hasNode(String nodeId) {
        return nodes.containsKey(nodeId);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getEdgeCount() {
        return adjacencyList.values().stream().mapToInt(List::size).sum();
    }

    public Collection<TravelNode> getAllNodes() {
        return nodes.values();
    }

    public List<TravelEdge> getAllEdges() {
        List<TravelEdge> all = new ArrayList<>();
        adjacencyList.values().forEach(all::addAll);
        return all;
    }

    public Map<String, TravelNode> getNodes() {
        return nodes;
    }

    public Map<String, List<TravelEdge>> getAdjacencyList() {
        return adjacencyList;
    }

    public double estimateHaversineDistanceKm(String fromNodeId, String toNodeId) {
        TravelNode from = nodes.get(fromNodeId);
        TravelNode to = nodes.get(toNodeId);

        if (from == null || to == null || (from.getLatitude() == 0 && from.getLongitude() == 0)
                || (to.getLatitude() == 0 && to.getLongitude() == 0)) {
            return 0.0;
        }

        double earthRadius = 6371.0;
        double dLat = Math.toRadians(to.getLatitude() - from.getLatitude());
        double dLon = Math.toRadians(to.getLongitude() - from.getLongitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(from.getLatitude())) * Math.cos(Math.toRadians(to.getLatitude()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
